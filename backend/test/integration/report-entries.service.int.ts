import { Test, TestingModule } from '@nestjs/testing';
import { getRepositoryToken } from '@nestjs/typeorm';
import { DataSource } from 'typeorm';
import { ReportEntriesService } from '../../src/report-entries/report-entries.service';
import { ReportEntry } from '../../src/entities/report-entry.entity';
import { Weight } from '../../src/entities/weight.entity';
import { FoodEaten } from '../../src/entities/food-eaten.entity';
import { ExercisePerformed } from '../../src/entities/exercise-performed.entity';
import { TestDataFactory } from './helpers/test-data.factory';
import {
  createTestDataSource,
  destroyTestDataSource,
} from './helpers/test-database.helper';

describe('ReportEntriesService (Integration)', () => {
  let dataSource: DataSource;
  let service: ReportEntriesService;
  let factory: TestDataFactory;

  beforeAll(async () => {
    dataSource = await createTestDataSource();
    factory = new TestDataFactory(dataSource);

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        ReportEntriesService,
        {
          provide: getRepositoryToken(ReportEntry),
          useValue: dataSource.getRepository(ReportEntry),
        },
        {
          provide: getRepositoryToken(Weight),
          useValue: dataSource.getRepository(Weight),
        },
        {
          provide: getRepositoryToken(FoodEaten),
          useValue: dataSource.getRepository(FoodEaten),
        },
        {
          provide: getRepositoryToken(ExercisePerformed),
          useValue: dataSource.getRepository(ExercisePerformed),
        },
        {
          provide: DataSource,
          useValue: dataSource,
        },
      ],
    }).compile();

    service = module.get<ReportEntriesService>(ReportEntriesService);
  });

  afterAll(async () => {
    await destroyTestDataSource(dataSource);
  });

  afterEach(async () => {
    // Use query builder to delete all rows (TypeORM doesn't allow delete({}))
    await dataSource.createQueryBuilder().delete().from(ReportEntry).execute();
    await dataSource.createQueryBuilder().delete().from(ExercisePerformed).execute();
    await dataSource.createQueryBuilder().delete().from(FoodEaten).execute();
    await dataSource.createQueryBuilder().delete().from(Weight).execute();
  });

  describe('updateFromDate - Basic Report Calculation', () => {
    it('should calculate report entries with dynamic test data', async () => {
      // Create user with 7 days of weight history
      const user = await factory.createUser();
      await factory.createWeightHistory(user, 7, 180);

      // Create foods and food logs
      const foods = await factory.createGlobalFoods();
      await factory.createFoodLogs(user, foods, 7);

      // Create exercises and exercise logs
      const exercises = await factory.createGlobalExercises();
      await factory.createExerciseLogs(user, exercises, 7);

      // Calculate start date (7 days ago)
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const sevenDaysAgo = new Date(today);
      sevenDaysAgo.setDate(today.getDate() - 7);

      // Run report calculation
      await service.updateFromDate(user.id, sevenDaysAgo);

      // Verify report entries were created
      const reportEntries = await service.findByUserAndDateRange(
        user.id,
        sevenDaysAgo,
        today,
      );

      expect(reportEntries.length).toBeGreaterThan(0);

      // Verify each report entry has required data
      for (const entry of reportEntries) {
        expect(entry.pounds).toBeGreaterThan(0);
        expect(entry.netCalories).toBeDefined();
        expect(entry.date).toBeDefined();
      }
    });

    it('should calculate correct net calories from food and exercise', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      // Create weight for today
      const weightRepo = dataSource.getRepository(Weight);
      await weightRepo.save(
        weightRepo.create({
          user,
          date: today,
          pounds: 180,
        }),
      );

      // Create a food with known calories
      const foodRepo = dataSource.getRepository(Weight);
      const food = await factory.createCustomFood(user, {
        name: 'Test Food',
        calories: 500,
        defaultServingType: 1, // OUNCE
        servingTypeQty: 1,
      });

      // Create food eaten: 2 servings = 1000 calories
      const foodEatenRepo = dataSource.getRepository(FoodEaten);
      await foodEatenRepo.save(
        foodEatenRepo.create({
          user,
          food,
          date: today,
          servingType: 1, // OUNCE
          servingQty: 2,
        }),
      );

      // Create an exercise with known MET
      const exerciseRepo = dataSource.getRepository(ExercisePerformed);
      const exercise = await factory.createGlobalExercises();
      const walkingExercise = exercise.find((e) => e.metabolicEquivalent === 3.5);

      // Create exercise performed: 30 min walking at 180 lbs
      // Calories burned = 3.5 × 3.5 × (180/2.2) ÷ 200 × 30 ≈ 125
      await exerciseRepo.save(
        exerciseRepo.create({
          user,
          exercise: walkingExercise,
          date: today,
          minutes: 30,
        }),
      );

      // Run report calculation
      await service.updateFromDate(user.id, today);

      // Verify report entry
      const reportEntries = await service.findByUserAndDateRange(user.id, today, today);

      expect(reportEntries.length).toBe(1);
      expect(reportEntries[0].pounds).toBe(180);

      // Net calories should be ~1000 (food) - 125 (exercise) = 875
      expect(reportEntries[0].netCalories).toBeGreaterThanOrEqual(850);
      expect(reportEntries[0].netCalories).toBeLessThan(900);
    });
  });

  describe('updateFromDate - Weight Gap Filling', () => {
    it('should use most recent weight when no weight exists for current date', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      const threeDaysAgo = new Date(today);
      threeDaysAgo.setDate(today.getDate() - 3);

      // Create weight only 3 days ago
      const weightRepo = dataSource.getRepository(Weight);
      await weightRepo.save(
        weightRepo.create({
          user,
          date: threeDaysAgo,
          pounds: 185,
        }),
      );

      // No foods or exercises

      // Run report calculation for today
      await service.updateFromDate(user.id, today);

      // Verify report entry uses weight from 3 days ago
      const reportEntries = await service.findByUserAndDateRange(user.id, today, today);

      expect(reportEntries.length).toBe(1);
      expect(reportEntries[0].pounds).toBe(185); // Uses weight from 3 days ago
      expect(reportEntries[0].netCalories).toBe(0); // No food or exercise
    });

    it('should skip dates when no weight is found at all', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      // No weights created at all

      // Run report calculation
      await service.updateFromDate(user.id, today);

      // Verify no report entries created
      const reportEntries = await service.findByUserAndDateRange(user.id, today, today);

      expect(reportEntries.length).toBe(0);
    });
  });

  describe('updateFromDate - Multiple Date Processing', () => {
    it('should process all dates from startDate to today', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      const fiveDaysAgo = new Date(today);
      fiveDaysAgo.setDate(today.getDate() - 5);

      // Create weights for all 5 days
      await factory.createWeightHistory(user, 5, 180);

      // Run report calculation
      await service.updateFromDate(user.id, fiveDaysAgo);

      // Verify report entries for all days
      const reportEntries = await service.findByUserAndDateRange(
        user.id,
        fiveDaysAgo,
        today,
      );

      // Should have entries for all days that have weights
      expect(reportEntries.length).toBeGreaterThanOrEqual(5);
    });

    it('should update existing report entries instead of creating duplicates', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      // Create weight
      const weightRepo = dataSource.getRepository(Weight);
      await weightRepo.save(
        weightRepo.create({
          user,
          date: today,
          pounds: 180,
        }),
      );

      // First calculation - creates entry
      await service.updateFromDate(user.id, today);

      let reportEntries = await service.findByUserAndDateRange(user.id, today, today);
      expect(reportEntries.length).toBe(1);
      const firstNetCalories = reportEntries[0].netCalories;

      // Add some food
      const foods = await factory.createGlobalFoods();
      const foodEatenRepo = dataSource.getRepository(FoodEaten);
      await foodEatenRepo.save(
        foodEatenRepo.create({
          user,
          food: foods[0],
          date: today,
          servingType: foods[0].defaultServingType,
          servingQty: 1,
        }),
      );

      // Second calculation - updates entry
      await service.updateFromDate(user.id, today);

      reportEntries = await service.findByUserAndDateRange(user.id, today, today);

      // Should still be only 1 entry (updated, not duplicated)
      expect(reportEntries.length).toBe(1);

      // Net calories should have changed
      expect(reportEntries[0].netCalories).not.toBe(firstNetCalories);
      expect(reportEntries[0].netCalories).toBeGreaterThan(firstNetCalories);
    });
  });

  describe('updateFromDate - Transaction Handling', () => {
    it('should handle transactions correctly within existing transaction', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      // Create weight
      const weightRepo = dataSource.getRepository(Weight);
      await weightRepo.save(
        weightRepo.create({
          user,
          date: today,
          pounds: 180,
        }),
      );

      // Create external transaction
      const queryRunner = dataSource.createQueryRunner();
      await queryRunner.connect();
      await queryRunner.startTransaction();

      try {
        // Call updateFromDate with existing QueryRunner
        await service.updateFromDate(user.id, today, queryRunner);

        await queryRunner.commitTransaction();
      } catch (error) {
        await queryRunner.rollbackTransaction();
        throw error;
      } finally {
        await queryRunner.release();
      }

      // Verify report entry was created
      const reportEntries = await service.findByUserAndDateRange(user.id, today, today);
      expect(reportEntries.length).toBe(1);
    });
  });

  describe('updateFromDate - Complex Scenarios', () => {
    it('should handle multiple foods and exercises in single day', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      // Create weight
      const weightRepo = dataSource.getRepository(Weight);
      await weightRepo.save(
        weightRepo.create({
          user,
          date: today,
          pounds: 180,
        }),
      );

      // Create multiple foods eaten
      const foods = await factory.createGlobalFoods();
      const foodEatenRepo = dataSource.getRepository(FoodEaten);

      let totalCaloriesFromFood = 0;
      for (let i = 0; i < 3; i++) {
        const food = foods[i];
        await foodEatenRepo.save(
          foodEatenRepo.create({
            user,
            food,
            date: today,
            servingType: food.defaultServingType,
            servingQty: 1,
          }),
        );
        totalCaloriesFromFood += food.calories;
      }

      // Create multiple exercises
      const exercises = await factory.createGlobalExercises();
      const exercisePerformedRepo = dataSource.getRepository(ExercisePerformed);

      for (let i = 0; i < 2; i++) {
        const exercise = exercises[i];
        await exercisePerformedRepo.save(
          exercisePerformedRepo.create({
            user,
            exercise,
            date: today,
            minutes: 30,
          }),
        );
      }

      // Run report calculation
      await service.updateFromDate(user.id, today);

      // Verify report entry
      const reportEntries = await service.findByUserAndDateRange(user.id, today, today);

      expect(reportEntries.length).toBe(1);
      expect(reportEntries[0].netCalories).toBeLessThan(totalCaloriesFromFood); // Exercise reduced net calories
    });

    it('should handle user with complete 30-day profile', async () => {
      // This tests the most realistic scenario
      const { user } = await factory.createCompleteUserProfile(30);

      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const thirtyDaysAgo = new Date(today);
      thirtyDaysAgo.setDate(today.getDate() - 30);

      // Run report calculation
      await service.updateFromDate(user.id, thirtyDaysAgo);

      // Verify report entries
      const reportEntries = await service.findByUserAndDateRange(
        user.id,
        thirtyDaysAgo,
        today,
      );

      // Should have entries for most/all days
      expect(reportEntries.length).toBeGreaterThan(25);

      // Verify all entries have valid data
      for (const entry of reportEntries) {
        expect(entry.pounds).toBeGreaterThan(0);
        expect(entry.netCalories).toBeDefined();
      }
    });
  });
});
