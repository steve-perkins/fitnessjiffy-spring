import { Injectable, Inject, forwardRef } from '@nestjs/common';
import { InjectRepository, InjectDataSource } from '@nestjs/typeorm';
import {
  Repository,
  DataSource,
  QueryRunner,
  LessThanOrEqual,
  Between,
} from 'typeorm';
import { ReportEntry } from '../entities/report-entry.entity';
import { Weight } from '../entities/weight.entity';
import { FoodEaten } from '../entities/food-eaten.entity';
import { ExercisePerformed } from '../entities/exercise-performed.entity';

@Injectable()
export class ReportEntriesService {
  constructor(
    @InjectRepository(ReportEntry)
    private reportEntryRepository: Repository<ReportEntry>,
    @InjectRepository(Weight)
    private weightRepository: Repository<Weight>,
    @InjectRepository(FoodEaten)
    private foodEatenRepository: Repository<FoodEaten>,
    @InjectRepository(ExercisePerformed)
    private exercisePerformedRepository: Repository<ExercisePerformed>,
    @InjectDataSource()
    private dataSource: DataSource,
  ) {}

  /**
   * Find all report entries for a user, optionally filtered by date range
   * If startDate and endDate are provided, filters to that range
   * If neither provided, returns all report entries for the user
   */
  async findByUserAndDateRange(
    userId: string,
    startDate?: Date,
    endDate?: Date,
  ): Promise<ReportEntry[]> {
    const whereClause: any = {
      user: { id: userId },
    };

    // Add date range filter only if both dates provided
    if (startDate && endDate) {
      whereClause.date = Between(startDate, endDate);
    }

    return this.reportEntryRepository.find({
      where: whereClause,
      order: { date: 'ASC' },
    });
  }

  /**
   * CRITICAL: Report data calculation algorithm
   * Updates report_entries table synchronously for all dates from startDate to today.
   *
   * This method can be called:
   * 1. Within an existing transaction (via queryRunner parameter)
   * 2. As a standalone operation (creates its own transaction)
   *
   * Reference: ReportDataService.java:298-352
   *
   * @param userId - User ID
   * @param startDate - Start date for recalculation
   * @param queryRunner - Optional QueryRunner from parent transaction
   */
  async updateFromDate(
    userId: string,
    startDate: Date,
    queryRunner?: QueryRunner,
  ): Promise<void> {
    // Determine if we need to create our own transaction or use existing one
    const shouldManageTransaction = !queryRunner;
    const runner = queryRunner || this.dataSource.createQueryRunner();

    if (shouldManageTransaction) {
      await runner.connect();
      await runner.startTransaction();
    }

    try {
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      let currentDate = new Date(startDate);
      currentDate.setHours(0, 0, 0, 0);

      // Loop through each date from startDate to today
      while (currentDate <= today) {
        // 1. Find most recent weight on or before this date
        const weight = await runner.manager.findOne(Weight, {
          where: {
            user: { id: userId },
            date: LessThanOrEqual(currentDate),
          },
          order: { date: 'DESC' },
        });

        if (!weight) {
          // Skip this date if no weight found
          currentDate = new Date(currentDate);
          currentDate.setDate(currentDate.getDate() + 1);
          continue;
        }

        // 2. Sum all food calories for this date
        const foodsEaten = await runner.manager.find(FoodEaten, {
          where: {
            user: { id: userId },
            date: currentDate,
          },
        });

        let netCalories = 0;
        for (const foodEaten of foodsEaten) {
          netCalories += foodEaten.getCalories();
        }

        // 3. Calculate exercise calories burned for this date
        const exercisesPerformed = await runner.manager.find(
          ExercisePerformed,
          {
            where: {
              user: { id: userId },
              date: currentDate,
            },
          },
        );

        for (const exercisePerformed of exercisesPerformed) {
          netCalories -= exercisePerformed.getCaloriesBurned(weight.pounds);
        }

        // 4. Create or update report entry
        let reportEntry = await runner.manager.findOne(ReportEntry, {
          where: {
            user: { id: userId },
            date: currentDate,
          },
        });

        if (!reportEntry) {
          // Create new entry
          reportEntry = runner.manager.create(ReportEntry, {
            user: { id: userId },
            date: new Date(currentDate),
            pounds: weight.pounds,
            netCalories,
          });
        } else {
          // Update existing entry
          reportEntry.pounds = weight.pounds;
          reportEntry.netCalories = netCalories;
        }

        await runner.manager.save(ReportEntry, reportEntry);

        // Move to next date
        currentDate = new Date(currentDate);
        currentDate.setDate(currentDate.getDate() + 1);
      }

      // Commit only if we created the transaction
      if (shouldManageTransaction) {
        await runner.commitTransaction();
      }
    } catch (error) {
      // Rollback only if we created the transaction
      if (shouldManageTransaction) {
        await runner.rollbackTransaction();
      }
      throw error;
    } finally {
      // Release only if we created the transaction
      if (shouldManageTransaction) {
        await runner.release();
      }
    }
  }
}
