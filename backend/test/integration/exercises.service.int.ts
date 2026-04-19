import { Test, TestingModule } from '@nestjs/testing';
import { getRepositoryToken } from '@nestjs/typeorm';
import { DataSource } from 'typeorm';
import { ExercisesService } from '../../src/exercises/exercises.service';
import { Exercise } from '../../src/entities/exercise.entity';
import { ExercisePerformed } from '../../src/entities/exercise-performed.entity';
import { Weight } from '../../src/entities/weight.entity';
import { ReportEntry } from '../../src/entities/report-entry.entity';
import { FoodEaten } from '../../src/entities/food-eaten.entity';
import { ReportEntriesService } from '../../src/report-entries/report-entries.service';
import { TestDataFactory } from './helpers/test-data.factory';
import {
  createTestDataSource,
  destroyTestDataSource,
} from './helpers/test-database.helper';

describe('ExercisesService (Integration)', () => {
  let dataSource: DataSource;
  let exercisesService: ExercisesService;
  let reportEntriesService: ReportEntriesService;
  let factory: TestDataFactory;

  beforeAll(async () => {
    dataSource = await createTestDataSource();
    factory = new TestDataFactory(dataSource);

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        ExercisesService,
        ReportEntriesService,
        {
          provide: getRepositoryToken(Exercise),
          useValue: dataSource.getRepository(Exercise),
        },
        {
          provide: getRepositoryToken(ExercisePerformed),
          useValue: dataSource.getRepository(ExercisePerformed),
        },
        {
          provide: getRepositoryToken(Weight),
          useValue: dataSource.getRepository(Weight),
        },
        {
          provide: getRepositoryToken(ReportEntry),
          useValue: dataSource.getRepository(ReportEntry),
        },
        {
          provide: getRepositoryToken(FoodEaten),
          useValue: dataSource.getRepository(FoodEaten),
        },
        {
          provide: DataSource,
          useValue: dataSource,
        },
      ],
    }).compile();

    exercisesService = module.get<ExercisesService>(ExercisesService);
    reportEntriesService = module.get<ReportEntriesService>(ReportEntriesService);
  });

  afterAll(async () => {
    await destroyTestDataSource(dataSource);
  });

  afterEach(async () => {
    await dataSource.createQueryBuilder().delete().from(ReportEntry).execute();
    await dataSource.createQueryBuilder().delete().from(ExercisePerformed).execute();
    await dataSource.createQueryBuilder().delete().from(FoodEaten).execute();
    await dataSource.createQueryBuilder().delete().from(Weight).execute();
    await dataSource.createQueryBuilder().delete().from('users').execute();
  });

  describe('addExercisePerformed - Report Entry Triggering', () => {
    it('should trigger report updates synchronously within transaction', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      // Create weight
      await factory.createWeightHistory(user, 1, 180);

      // Create exercise
      const exercises = await factory.createGlobalExercises();
      const walkingExercise = exercises.find((e) => e.metabolicEquivalent === 3.5);

      // Add exercise performed
      await exercisesService.addExercisePerformed(user.id, {
        exerciseId: walkingExercise!.id,
        date: today.toISOString(),
        minutes: 30,
      });

      // Verify report entry was created/updated automatically
      const reports = await reportEntriesService.findByUserAndDateRange(
        user.id,
        today,
        today,
      );

      expect(reports.length).toBe(1);
      expect(reports[0].netCalories).toBeLessThan(0); // Negative because exercise burns calories
    });

    it('should reduce net calories in report when exercise is added', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      // Create weight
      await factory.createWeightHistory(user, 1, 180);

      // Generate initial report (0 calories)
      await reportEntriesService.updateFromDate(user.id, today);

      const initialReports = await reportEntriesService.findByUserAndDateRange(
        user.id,
        today,
        today,
      );
      expect(initialReports[0].netCalories).toBe(0);

      // Add exercise
      const exercises = await factory.createGlobalExercises();
      const walkingExercise = exercises.find((e) => e.metabolicEquivalent === 3.5);

      await exercisesService.addExercisePerformed(user.id, {
        exerciseId: walkingExercise!.id,
        date: today.toISOString(),
        minutes: 30,
      });

      // Verify calories reduced
      const updatedReports = await reportEntriesService.findByUserAndDateRange(
        user.id,
        today,
        today,
      );
      expect(updatedReports[0].netCalories).toBeLessThan(0);
    });

    it('should rollback on transaction error', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      await factory.createWeightHistory(user, 1, 180);

      const exercises = await factory.createGlobalExercises();
      const exercise = exercises[0];

      // Add exercise first time (success)
      await exercisesService.addExercisePerformed(user.id, {
        exerciseId: exercise.id,
        date: today.toISOString(),
        minutes: 30,
      });

      // Try to add same exercise again (should fail - duplicate)
      await expect(
        exercisesService.addExercisePerformed(user.id, {
          exerciseId: exercise.id,
          date: today.toISOString(),
          minutes: 45,
        }),
      ).rejects.toThrow();

      // Verify only one ExercisePerformed entry exists
      const exercisesPerformed = await dataSource.getRepository(ExercisePerformed).find({
        where: { user: { id: user.id } },
      });

      expect(exercisesPerformed.length).toBe(1);
      expect(exercisesPerformed[0].minutes).toBe(30); // Original, not updated
    });
  });

  describe('updateExercisePerformed - Report Entry Triggering', () => {
    it('should trigger report updates when exercise duration changes', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      await factory.createWeightHistory(user, 1, 180);

      const exercises = await factory.createGlobalExercises();
      const walkingExercise = exercises.find((e) => e.metabolicEquivalent === 3.5);

      // Add exercise (30 minutes)
      const exercisePerformed = await exercisesService.addExercisePerformed(user.id, {
        exerciseId: walkingExercise!.id,
        date: today.toISOString(),
        minutes: 30,
      });

      const initialReports = await reportEntriesService.findByUserAndDateRange(
        user.id,
        today,
        today,
      );
      const initialCalories = initialReports[0].netCalories;

      // Update to 60 minutes (should burn more calories)
      await exercisesService.updateExercisePerformed(exercisePerformed.id, user.id, {
        minutes: 60,
      });

      const updatedReports = await reportEntriesService.findByUserAndDateRange(
        user.id,
        today,
        today,
      );
      const updatedCalories = updatedReports[0].netCalories;

      // Net calories should be more negative (more calories burned)
      expect(updatedCalories).toBeLessThan(initialCalories);
    });

    it('should update report entries synchronously within transaction', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      await factory.createWeightHistory(user, 1, 180);

      const exercises = await factory.createGlobalExercises();
      const exercise = exercises[0];

      const exercisePerformed = await exercisesService.addExercisePerformed(user.id, {
        exerciseId: exercise.id,
        date: today.toISOString(),
        minutes: 30,
      });

      // Update exercise
      await exercisesService.updateExercisePerformed(exercisePerformed.id, user.id, {
        minutes: 45,
      });

      // Verify both ExercisePerformed and ReportEntry updated
      const updated = await dataSource
        .getRepository(ExercisePerformed)
        .findOne({ where: { id: exercisePerformed.id } });

      expect(updated!.minutes).toBe(45);

      const reports = await reportEntriesService.findByUserAndDateRange(
        user.id,
        today,
        today,
      );
      expect(reports.length).toBe(1);
    });
  });

  describe('deleteExercisePerformed - Report Entry Triggering', () => {
    it('should trigger report updates when exercise is deleted', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      await factory.createWeightHistory(user, 1, 180);

      const exercises = await factory.createGlobalExercises();
      const walkingExercise = exercises.find((e) => e.metabolicEquivalent === 3.5);

      // Add exercise
      const exercisePerformed = await exercisesService.addExercisePerformed(user.id, {
        exerciseId: walkingExercise!.id,
        date: today.toISOString(),
        minutes: 30,
      });

      const initialReports = await reportEntriesService.findByUserAndDateRange(
        user.id,
        today,
        today,
      );
      expect(initialReports[0].netCalories).toBeLessThan(0);

      // Delete exercise
      await exercisesService.deleteExercisePerformed(exercisePerformed.id, user.id);

      // Verify net calories back to 0 (no exercise)
      const updatedReports = await reportEntriesService.findByUserAndDateRange(
        user.id,
        today,
        today,
      );
      expect(updatedReports[0].netCalories).toBe(0);
    });

    it('should delete exercise and update report synchronously within transaction', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      await factory.createWeightHistory(user, 1, 180);

      const exercises = await factory.createGlobalExercises();
      const exercise = exercises[0];

      const exercisePerformed = await exercisesService.addExercisePerformed(user.id, {
        exerciseId: exercise.id,
        date: today.toISOString(),
        minutes: 30,
      });

      // Delete
      await exercisesService.deleteExercisePerformed(exercisePerformed.id, user.id);

      // Verify exercise deleted
      const deleted = await dataSource
        .getRepository(ExercisePerformed)
        .findOne({ where: { id: exercisePerformed.id } });

      expect(deleted).toBeNull();

      // Verify report updated
      const reports = await reportEntriesService.findByUserAndDateRange(
        user.id,
        today,
        today,
      );
      expect(reports[0].netCalories).toBe(0);
    });
  });

  describe('Multiple Exercises - Combined Effect', () => {
    it('should correctly calculate net calories with multiple exercises', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      await factory.createWeightHistory(user, 1, 180);

      const exercises = await factory.createGlobalExercises();
      const walkingExercise = exercises.find((e) => e.metabolicEquivalent === 3.5);
      const runningExercise = exercises.find((e) => e.metabolicEquivalent === 8.0);

      // Add walking (30 min)
      await exercisesService.addExercisePerformed(user.id, {
        exerciseId: walkingExercise!.id,
        date: today.toISOString(),
        minutes: 30,
      });

      const afterWalking = await reportEntriesService.findByUserAndDateRange(
        user.id,
        today,
        today,
      );
      const caloriesAfterWalking = afterWalking[0].netCalories;

      // Add running (20 min)
      await exercisesService.addExercisePerformed(user.id, {
        exerciseId: runningExercise!.id,
        date: today.toISOString(),
        minutes: 20,
      });

      const afterRunning = await reportEntriesService.findByUserAndDateRange(
        user.id,
        today,
        today,
      );
      const caloriesAfterRunning = afterRunning[0].netCalories;

      // Should burn more calories with both exercises
      expect(caloriesAfterRunning).toBeLessThan(caloriesAfterWalking);
    });
  });

  describe('findPerformedOnDate', () => {
    it('should return exercises performed on specific date', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      const yesterday = new Date(today);
      yesterday.setDate(today.getDate() - 1);

      await factory.createWeightHistory(user, 2, 180);

      const exercises = await factory.createGlobalExercises();
      const exercise1 = exercises[0];
      const exercise2 = exercises[1];

      // Add exercises on different days
      await exercisesService.addExercisePerformed(user.id, {
        exerciseId: exercise1.id,
        date: today.toISOString(),
        minutes: 30,
      });

      await exercisesService.addExercisePerformed(user.id, {
        exerciseId: exercise2.id,
        date: yesterday.toISOString(),
        minutes: 45,
      });

      // Query for today
      const todayExercises = await exercisesService.findPerformedOnDate(user.id, today);

      expect(todayExercises.length).toBe(1);
      expect(todayExercises[0].minutes).toBe(30);

      // Query for yesterday
      const yesterdayExercises = await exercisesService.findPerformedOnDate(
        user.id,
        yesterday,
      );

      expect(yesterdayExercises.length).toBe(1);
      expect(yesterdayExercises[0].minutes).toBe(45);
    });
  });
});
