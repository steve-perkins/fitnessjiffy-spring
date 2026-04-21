import {
  Injectable,
  NotFoundException,
  ConflictException,
  ForbiddenException,
} from '@nestjs/common';
import { InjectRepository, InjectDataSource } from '@nestjs/typeorm';
import { Repository, DataSource, IsNull } from 'typeorm';
import { Food } from '../entities/food.entity';
import { FoodEaten } from '../entities/food-eaten.entity';
import { ReportEntriesService } from '../report-entries/report-entries.service';
import { CreateFoodDto } from './dto/create-food.dto';
import { UpdateFoodDto } from './dto/update-food.dto';
import { CreateFoodEatenDto } from './dto/create-food-eaten.dto';
import { UpdateFoodEatenDto } from './dto/update-food-eaten.dto';

@Injectable()
export class FoodsService {
  constructor(
    @InjectRepository(Food)
    private foodRepository: Repository<Food>,
    @InjectRepository(FoodEaten)
    private foodEatenRepository: Repository<FoodEaten>,
    private reportEntriesService: ReportEntriesService,
    @InjectDataSource()
    private dataSource: DataSource,
  ) {}

  /**
   * Find all foods visible to the user.
   * Includes user-owned foods and global foods (owner=null),
   * BUT excludes global foods if user has custom food with same name.
   *
   * Reference: FoodRepository.java:33-41
   */
  async findVisibleByUser(userId: string): Promise<Food[]> {
    return this.foodRepository
      .createQueryBuilder('food')
      .where('food.owner_id = :userId', { userId })
      .orWhere((qb) => {
        const subQuery = qb
          .subQuery()
          .select('1')
          .from(Food, 'subFood')
          .where('subFood.owner_id = :userId')
          .andWhere('subFood.name = food.name')
          .getQuery();
        return 'food.owner_id IS NULL AND NOT EXISTS ' + subQuery;
      })
      .setParameter('userId', userId)
      .orderBy('food.name', 'ASC')
      .getMany();
  }

  /**
   * Search foods with pg_trgm fuzzy matching.
   * Applies same visibility logic as findVisibleByUser.
   *
   * Reference: FoodRepository.java:43-57 (but using pg_trgm instead of LIKE)
   */
  async searchFoods(userId: string, query: string): Promise<Food[]> {
    return this.foodRepository
      .createQueryBuilder('food')
      .where(
        `(food.owner_id = :userId OR (food.owner_id IS NULL AND NOT EXISTS (
          SELECT 1 FROM foods subFood
          WHERE subFood.owner_id = :userId AND subFood.name = food.name
        )))`,
      )
      .andWhere(`similarity(food.name, :query) > 0.2`)
      .setParameter('userId', userId)
      .setParameter('query', query)
      .orderBy('similarity(food.name, :query)', 'DESC')
      .addOrderBy('food.name', 'ASC')
      .limit(50)
      .getMany();
  }

  /**
   * Create a custom food owned by the user
   */
  async createFood(
    userId: string,
    createFoodDto: CreateFoodDto,
  ): Promise<Food> {
    // Check for duplicate name
    const existing = await this.foodRepository.findOne({
      where: {
        owner: { id: userId },
        name: createFoodDto.name,
      },
    });

    if (existing) {
      throw new ConflictException(
        `You already have a custom food named "${createFoodDto.name}"`,
      );
    }

    const now = new Date();
    const food = this.foodRepository.create({
      ...createFoodDto,
      owner: { id: userId },
      createdTime: now,
      lastUpdatedTime: now,
    });

    return this.foodRepository.save(food);
  }

  /**
   * Update a food. Can update user-owned foods, or create user-owned copy of global food.
   * Reference: FoodService.java:147-199
   */
  async updateFood(
    foodId: string,
    userId: string,
    updateFoodDto: UpdateFoodDto,
  ): Promise<Food> {
    const food = await this.foodRepository.findOne({
      where: { id: foodId },
      relations: ['owner'],
    });

    if (!food) {
      throw new NotFoundException(`Food with ID ${foodId} not found`);
    }

    // Can only update user-owned foods or create copy of global food
    if (food.owner && food.owner.id !== userId) {
      throw new ForbiddenException(
        'You cannot modify another user\u2019s custom food',
      );
    }

    // Find earliest date user ate this food (for report entry updates)
    const foodsEatenSortedByDate = await this.foodEatenRepository.find({
      where: {
        user: { id: userId },
        food: { id: foodId },
      },
      order: { date: 'ASC' },
    });

    const dateFirstEaten =
      foodsEatenSortedByDate.length > 0
        ? foodsEatenSortedByDate[0].date
        : new Date();

    // If updating global food, create user-owned copy
    if (!food.owner) {
      // Check for name conflicts
      if (updateFoodDto.name) {
        const conflict = await this.foodRepository.findOne({
          where: {
            owner: { id: userId },
            name: updateFoodDto.name,
          },
        });
        if (conflict) {
          throw new ConflictException(
            `You already have a custom food named "${updateFoodDto.name}"`,
          );
        }
      }

      // Create new user-owned food
      const newFood = this.foodRepository.create({
        name: food.name,
        defaultServingType: food.defaultServingType,
        servingTypeQty: food.servingTypeQty,
        calories: food.calories,
        fat: food.fat,
        saturatedFat: food.saturatedFat,
        carbs: food.carbs,
        fiber: food.fiber,
        sugar: food.sugar,
        protein: food.protein,
        sodium: food.sodium,
        ...updateFoodDto,
        owner: { id: userId },
      });

      const saved = await this.foodRepository.save(newFood);

      // Update report entries from the earliest date this food was eaten
      await this.reportEntriesService.updateFromDate(userId, dateFirstEaten);

      return saved;
    }

    // Update user-owned food
    if (updateFoodDto.name && updateFoodDto.name !== food.name) {
      // Check for name conflicts
      const conflict = await this.foodRepository.findOne({
        where: {
          owner: { id: userId },
          name: updateFoodDto.name,
        },
      });
      if (conflict && conflict.id !== foodId) {
        throw new ConflictException(
          `You already have a custom food named "${updateFoodDto.name}"`,
        );
      }
    }

    Object.assign(food, updateFoodDto);
    const saved = await this.foodRepository.save(food);

    // Update report entries from the earliest date this food was eaten
    await this.reportEntriesService.updateFromDate(userId, dateFirstEaten);

    return saved;
  }

  /**
   * Delete a user-owned food
   */
  async deleteFood(foodId: string, userId: string): Promise<void> {
    const food = await this.foodRepository.findOne({
      where: { id: foodId, owner: { id: userId } },
    });

    if (!food) {
      throw new NotFoundException(
        `Food with ID ${foodId} not found or not owned by you`,
      );
    }

    // Find earliest date user ate this food (for report entry updates)
    const foodsEatenSortedByDate = await this.foodEatenRepository.find({
      where: {
        user: { id: userId },
        food: { id: foodId },
      },
      order: { date: 'ASC' },
    });

    const dateFirstEaten =
      foodsEatenSortedByDate.length > 0
        ? foodsEatenSortedByDate[0].date
        : new Date();

    await this.foodRepository.remove(food);

    // Update report entries from the earliest date this food was eaten
    await this.reportEntriesService.updateFromDate(userId, dateFirstEaten);
  }

  /**
   * Find foods eaten by user on a specific date
   */
  async findEatenOnDate(userId: string, date: Date): Promise<FoodEaten[]> {
    return this.foodEatenRepository.find({
      where: {
        user: { id: userId },
        date,
      },
      relations: ['food'],
    });
  }

  /**
   * Find foods eaten by user within a date range
   */
  async findEatenInRange(
    userId: string,
    startDate: Date,
    endDate: Date,
  ): Promise<FoodEaten[]> {
    return this.foodEatenRepository
      .createQueryBuilder('foodEaten')
      .leftJoinAndSelect('foodEaten.food', 'food')
      .where('"foodEaten"."user_id" = :userId', { userId })
      .andWhere('foodEaten.date >= :startDate', { startDate })
      .andWhere('foodEaten.date <= :endDate', { endDate })
      .orderBy('foodEaten.date', 'DESC')
      .addOrderBy('food.name', 'ASC')
      .getMany();
  }

  /**
   * Add a food eaten entry with synchronous report update.
   * Uses transaction to ensure data consistency.
   *
   * Reference: FoodService.java:88-109
   */
  async addFoodEaten(
    userId: string,
    createFoodEatenDto: CreateFoodEatenDto,
  ): Promise<FoodEaten> {
    const queryRunner = this.dataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();

    try {
      const date = new Date(createFoodEatenDto.date);

      // Check for duplicates
      const existing = await queryRunner.manager.findOne(FoodEaten, {
        where: {
          user: { id: userId },
          food: { id: createFoodEatenDto.foodId },
          date,
        },
      });

      if (existing) {
        throw new ConflictException(
          'You have already added this food for this date',
        );
      }

      // Get food to use its default serving
      const food = await queryRunner.manager.findOne(Food, {
        where: { id: createFoodEatenDto.foodId },
      });

      if (!food) {
        throw new NotFoundException(
          `Food with ID ${createFoodEatenDto.foodId} not found`,
        );
      }

      // Create food eaten entry
      const foodEaten = queryRunner.manager.create(FoodEaten, {
        user: { id: userId },
        food: { id: createFoodEatenDto.foodId },
        date,
        servingType: createFoodEatenDto.servingType,
        servingQty: createFoodEatenDto.servingQty,
      });

      const saved = await queryRunner.manager.save(FoodEaten, foodEaten);

      // Update report entries synchronously within this transaction
      await this.reportEntriesService.updateFromDate(
        userId,
        date,
        queryRunner,
      );

      await queryRunner.commitTransaction();
      return saved;
    } catch (error) {
      await queryRunner.rollbackTransaction();
      throw error;
    } finally {
      await queryRunner.release();
    }
  }

  /**
   * Update a food eaten entry with synchronous report update.
   * Reference: FoodService.java:111-121
   */
  async updateFoodEaten(
    foodEatenId: string,
    userId: string,
    updateFoodEatenDto: UpdateFoodEatenDto,
  ): Promise<FoodEaten> {
    const queryRunner = this.dataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();

    try {
      const foodEaten = await queryRunner.manager.findOne(FoodEaten, {
        where: { id: foodEatenId, user: { id: userId } },
      });

      if (!foodEaten) {
        throw new NotFoundException(
          `FoodEaten entry with ID ${foodEatenId} not found`,
        );
      }

      foodEaten.servingType = updateFoodEatenDto.servingType;
      foodEaten.servingQty = updateFoodEatenDto.servingQty;

      const saved = await queryRunner.manager.save(FoodEaten, foodEaten);

      // Update report entries synchronously within this transaction
      await this.reportEntriesService.updateFromDate(
        userId,
        foodEaten.date,
        queryRunner,
      );

      await queryRunner.commitTransaction();
      return saved;
    } catch (error) {
      await queryRunner.rollbackTransaction();
      throw error;
    } finally {
      await queryRunner.release();
    }
  }

  /**
   * Delete a food eaten entry with synchronous report update.
   * Reference: FoodService.java:123-127
   */
  async deleteFoodEaten(
    foodEatenId: string,
    userId: string,
  ): Promise<void> {
    const queryRunner = this.dataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();

    try {
      const foodEaten = await queryRunner.manager.findOne(FoodEaten, {
        where: { id: foodEatenId, user: { id: userId } },
      });

      if (!foodEaten) {
        throw new NotFoundException(
          `FoodEaten entry with ID ${foodEatenId} not found`,
        );
      }

      const date = foodEaten.date;

      await queryRunner.manager.remove(FoodEaten, foodEaten);

      // Update report entries synchronously within this transaction
      await this.reportEntriesService.updateFromDate(userId, date, queryRunner);

      await queryRunner.commitTransaction();
    } catch (error) {
      await queryRunner.rollbackTransaction();
      throw error;
    } finally {
      await queryRunner.release();
    }
  }
}
