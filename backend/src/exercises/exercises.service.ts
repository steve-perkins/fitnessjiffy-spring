import {
  Injectable,
  NotFoundException,
  ConflictException,
} from '@nestjs/common';
import { InjectRepository, InjectDataSource } from '@nestjs/typeorm';
import { Repository, DataSource } from 'typeorm';
import { Exercise } from '../entities/exercise.entity';
import { ExercisePerformed } from '../entities/exercise-performed.entity';
import { ReportEntriesService } from '../report-entries/report-entries.service';
import { CreateExercisePerformedDto } from './dto/create-exercise-performed.dto';
import { UpdateExercisePerformedDto } from './dto/update-exercise-performed.dto';

@Injectable()
export class ExercisesService {
  constructor(
    @InjectRepository(Exercise)
    private exerciseRepository: Repository<Exercise>,
    @InjectRepository(ExercisePerformed)
    private exercisePerformedRepository: Repository<ExercisePerformed>,
    private reportEntriesService: ReportEntriesService,
    @InjectDataSource()
    private dataSource: DataSource,
  ) {}

  /**
   * Get all distinct exercise categories
   */
  async findAllCategories(): Promise<string[]> {
    const result = await this.exerciseRepository
      .createQueryBuilder('exercise')
      .select('DISTINCT(exercise.category)', 'category')
      .orderBy('exercise.category', 'ASC')
      .getRawMany();

    return result.map((row) => row.category);
  }

  /**
   * Find exercises in a specific category
   */
  async findByCategory(category: string): Promise<Exercise[]> {
    return this.exerciseRepository.find({
      where: { category },
      order: { description: 'ASC' },
    });
  }

  /**
   * Search exercises with pg_trgm fuzzy matching
   * Reference: ExerciseRepository.java:21-27 (but using pg_trgm)
   */
  async searchExercises(query: string): Promise<Exercise[]> {
    return this.exerciseRepository
      .createQueryBuilder('exercise')
      .where(`similarity(exercise.description, :query) > 0.3`)
      .setParameter('query', query)
      .orderBy('similarity(exercise.description, :query)', 'DESC')
      .addOrderBy('exercise.description', 'ASC')
      .limit(50)
      .getMany();
  }

  /**
   * Find exercises performed by user on a specific date
   */
  async findPerformedOnDate(
    userId: string,
    date: Date,
  ): Promise<ExercisePerformed[]> {
    return this.exercisePerformedRepository.find({
      where: {
        user: { id: userId },
        date,
      },
      relations: ['exercise'],
    });
  }

  /**
   * Find exercises performed by user within a date range
   */
  async findPerformedInRange(
    userId: string,
    startDate: Date,
    endDate: Date,
  ): Promise<ExercisePerformed[]> {
    return this.exercisePerformedRepository
      .createQueryBuilder('exercisePerformed')
      .leftJoinAndSelect('exercisePerformed.exercise', 'exercise')
      .where('exercisePerformed.userId = :userId', { userId })
      .andWhere('exercisePerformed.date >= :startDate', { startDate })
      .andWhere('exercisePerformed.date <= :endDate', { endDate })
      .orderBy('exercisePerformed.date', 'DESC')
      .addOrderBy('exercise.description', 'ASC')
      .getMany();
  }

  /**
   * Add an exercise performed entry with synchronous report update.
   * Reference: ExerciseService.java:112-132
   */
  async addExercisePerformed(
    userId: string,
    createExercisePerformedDto: CreateExercisePerformedDto,
  ): Promise<ExercisePerformed> {
    const queryRunner = this.dataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();

    try {
      const date = new Date(createExercisePerformedDto.date);

      // Check for duplicates
      const existing = await queryRunner.manager.findOne(ExercisePerformed, {
        where: {
          user: { id: userId },
          exercise: { id: createExercisePerformedDto.exerciseId },
          date,
        },
      });

      if (existing) {
        throw new ConflictException(
          'You have already added this exercise for this date',
        );
      }

      // Verify exercise exists
      const exercise = await queryRunner.manager.findOne(Exercise, {
        where: { id: createExercisePerformedDto.exerciseId },
      });

      if (!exercise) {
        throw new NotFoundException(
          `Exercise with ID ${createExercisePerformedDto.exerciseId} not found`,
        );
      }

      // Create exercise performed entry
      const exercisePerformed = queryRunner.manager.create(ExercisePerformed, {
        user: { id: userId },
        exercise: { id: createExercisePerformedDto.exerciseId },
        date,
        minutes: createExercisePerformedDto.minutes,
      });

      const saved = await queryRunner.manager.save(
        ExercisePerformed,
        exercisePerformed,
      );

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
   * Update an exercise performed entry with synchronous report update.
   * Reference: ExerciseService.java:134-142
   */
  async updateExercisePerformed(
    exercisePerformedId: string,
    userId: string,
    updateExercisePerformedDto: UpdateExercisePerformedDto,
  ): Promise<ExercisePerformed> {
    const queryRunner = this.dataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();

    try {
      const exercisePerformed = await queryRunner.manager.findOne(
        ExercisePerformed,
        {
          where: { id: exercisePerformedId, user: { id: userId } },
        },
      );

      if (!exercisePerformed) {
        throw new NotFoundException(
          `ExercisePerformed entry with ID ${exercisePerformedId} not found`,
        );
      }

      exercisePerformed.minutes = updateExercisePerformedDto.minutes;

      const saved = await queryRunner.manager.save(
        ExercisePerformed,
        exercisePerformed,
      );

      // Update report entries synchronously within this transaction
      await this.reportEntriesService.updateFromDate(
        userId,
        exercisePerformed.date,
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
   * Delete an exercise performed entry with synchronous report update.
   * Reference: ExerciseService.java:144-148
   */
  async deleteExercisePerformed(
    exercisePerformedId: string,
    userId: string,
  ): Promise<void> {
    const queryRunner = this.dataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();

    try {
      const exercisePerformed = await queryRunner.manager.findOne(
        ExercisePerformed,
        {
          where: { id: exercisePerformedId, user: { id: userId } },
        },
      );

      if (!exercisePerformed) {
        throw new NotFoundException(
          `ExercisePerformed entry with ID ${exercisePerformedId} not found`,
        );
      }

      const date = exercisePerformed.date;

      await queryRunner.manager.remove(ExercisePerformed, exercisePerformed);

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
