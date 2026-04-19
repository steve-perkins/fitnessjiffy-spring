import { Test, TestingModule } from '@nestjs/testing';
import { getRepositoryToken } from '@nestjs/typeorm';
import { DataSource } from 'typeorm';
import { WeightsService } from '../../src/weights/weights.service';
import { Weight } from '../../src/entities/weight.entity';
import { ReportEntry } from '../../src/entities/report-entry.entity';
import { ReportEntriesService } from '../../src/report-entries/report-entries.service';
import { FoodEaten } from '../../src/entities/food-eaten.entity';
import { ExercisePerformed } from '../../src/entities/exercise-performed.entity';
import { TestDataFactory } from './helpers/test-data.factory';
import {
  createTestDataSource,
  destroyTestDataSource,
} from './helpers/test-database.helper';

describe('WeightsService (Integration)', () => {
  let dataSource: DataSource;
  let weightsService: WeightsService;
  let reportEntriesService: ReportEntriesService;
  let factory: TestDataFactory;

  beforeAll(async () => {
    dataSource = await createTestDataSource();
    factory = new TestDataFactory(dataSource);

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        WeightsService,
        ReportEntriesService,
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
          provide: getRepositoryToken(ExercisePerformed),
          useValue: dataSource.getRepository(ExercisePerformed),
        },
        {
          provide: DataSource,
          useValue: dataSource,
        },
      ],
    }).compile();

    weightsService = module.get<WeightsService>(WeightsService);
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

  describe('create - Report Entry Triggering', () => {
    it('should trigger report updates when weight is created', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      // Create initial weight
      await weightsService.create(user.id, {
        date: today.toISOString(),
        pounds: 180,
      });

      // Verify report entry was created
      const reports = await reportEntriesService.findByUserAndDateRange(
        user.id,
        today,
        today,
      );

      expect(reports.length).toBe(1);
      expect(reports[0].pounds).toBe(180);
      expect(reports[0].netCalories).toBe(0); // No food or exercise yet
    });

    it('should trigger report updates from weight date forward', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      const threeDaysAgo = new Date(today);
      threeDaysAgo.setDate(today.getDate() - 3);

      // Create weight 3 days ago
      await weightsService.create(user.id, {
        date: threeDaysAgo.toISOString(),
        pounds: 180,
      });

      // Verify report entries created from 3 days ago to today
      const reports = await reportEntriesService.findByUserAndDateRange(
        user.id,
        threeDaysAgo,
        today,
      );

      expect(reports.length).toBeGreaterThan(0);
      // All report entries should have the same weight (gap-filling)
      expect(reports.every((r) => r.pounds === 180)).toBe(true);
    });

    it('should update existing report entries when new weight is added', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      const twoDaysAgo = new Date(today);
      twoDaysAgo.setDate(today.getDate() - 2);

      // Create initial weight 2 days ago
      await weightsService.create(user.id, {
        date: twoDaysAgo.toISOString(),
        pounds: 180,
      });

      const initialReports = await reportEntriesService.findByUserAndDateRange(
        user.id,
        twoDaysAgo,
        today,
      );
      expect(initialReports.every((r) => r.pounds === 180)).toBe(true);

      // Add new weight today
      await weightsService.create(user.id, {
        date: today.toISOString(),
        pounds: 175,
      });

      // Verify today's report now shows new weight
      const updatedReports = await reportEntriesService.findByUserAndDateRange(
        user.id,
        twoDaysAgo,
        today,
      );

      // Find today's report - should exist with updated weight
      expect(updatedReports.length).toBeGreaterThan(0);

      // The most recent report should be today with the new weight
      const sortedReports = updatedReports.sort((a, b) =>
        new Date(b.date).getTime() - new Date(a.date).getTime()
      );

      expect(sortedReports[0].pounds).toBe(175);
    });
  });

  describe('update - Report Entry Triggering', () => {
    it('should trigger report updates when weight is updated', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      // Create initial weight
      const weight = await weightsService.create(user.id, {
        date: today.toISOString(),
        pounds: 180,
      });

      const initialReports = await reportEntriesService.findByUserAndDateRange(
        user.id,
        today,
        today,
      );
      expect(initialReports[0].pounds).toBe(180);

      // Update weight
      await weightsService.update(weight.id, user.id, {
        pounds: 175,
      });

      // Verify report entry updated
      const updatedReports = await reportEntriesService.findByUserAndDateRange(
        user.id,
        today,
        today,
      );
      expect(updatedReports[0].pounds).toBe(175);
    });

    it('should update all report entries from updated weight date forward', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      const threeDaysAgo = new Date(today);
      threeDaysAgo.setDate(today.getDate() - 3);

      // Create weight 3 days ago
      const weight = await weightsService.create(user.id, {
        date: threeDaysAgo.toISOString(),
        pounds: 180,
      });

      const initialReports = await reportEntriesService.findByUserAndDateRange(
        user.id,
        threeDaysAgo,
        today,
      );
      expect(initialReports.every((r) => r.pounds === 180)).toBe(true);

      // Update that weight
      await weightsService.update(weight.id, user.id, {
        pounds: 175,
      });

      // Verify all report entries from that date forward updated
      const updatedReports = await reportEntriesService.findByUserAndDateRange(
        user.id,
        threeDaysAgo,
        today,
      );
      expect(updatedReports.every((r) => r.pounds === 175)).toBe(true);
    });
  });

  describe('findMostRecentOnDate - Gap Filling Logic', () => {
    it('should return weight from the exact date', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      await weightsService.create(user.id, {
        date: today.toISOString(),
        pounds: 180,
      });

      const weight = await weightsService.findMostRecentOnDate(user.id, today);

      expect(weight).toBeDefined();
      expect(weight!.pounds).toBe(180);
    });

    it('should return most recent weight when no weight exists for current date', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      const threeDaysAgo = new Date(today);
      threeDaysAgo.setDate(today.getDate() - 3);

      // Create weight 3 days ago
      await weightsService.create(user.id, {
        date: threeDaysAgo.toISOString(),
        pounds: 180,
      });

      // Query for today (no weight exists)
      const weight = await weightsService.findMostRecentOnDate(user.id, today);

      expect(weight).toBeDefined();
      expect(weight!.pounds).toBe(180); // Returns weight from 3 days ago
    });

    it('should return most recent weight when multiple weights exist', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      const fiveDaysAgo = new Date(today);
      fiveDaysAgo.setDate(today.getDate() - 5);

      const threeDaysAgo = new Date(today);
      threeDaysAgo.setDate(today.getDate() - 3);

      // Create two weights
      await weightsService.create(user.id, {
        date: fiveDaysAgo.toISOString(),
        pounds: 185,
      });

      await weightsService.create(user.id, {
        date: threeDaysAgo.toISOString(),
        pounds: 180,
      });

      // Query for today
      const weight = await weightsService.findMostRecentOnDate(user.id, today);

      expect(weight).toBeDefined();
      expect(weight!.pounds).toBe(180); // Returns most recent (3 days ago)
    });

    it('should return null when no weight exists on or before date', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      const tomorrow = new Date(today);
      tomorrow.setDate(today.getDate() + 1);

      // Create weight for tomorrow
      await weightsService.create(user.id, {
        date: tomorrow.toISOString(),
        pounds: 180,
      });

      // Query for today (weight is in future)
      const weight = await weightsService.findMostRecentOnDate(user.id, today);

      expect(weight).toBeNull();
    });
  });

  describe('findAllByUser', () => {
    it('should return all weights for user sorted by date desc', async () => {
      const user = await factory.createUser();

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      const yesterday = new Date(today);
      yesterday.setDate(today.getDate() - 1);

      const twoDaysAgo = new Date(today);
      twoDaysAgo.setDate(today.getDate() - 2);

      // Create weights in random order
      await weightsService.create(user.id, {
        date: today.toISOString(),
        pounds: 178,
      });

      await weightsService.create(user.id, {
        date: twoDaysAgo.toISOString(),
        pounds: 180,
      });

      await weightsService.create(user.id, {
        date: yesterday.toISOString(),
        pounds: 179,
      });

      const weights = await weightsService.findAllByUser(user.id);

      expect(weights.length).toBe(3);
      // Should be sorted by date DESC
      expect(weights[0].pounds).toBe(178); // today (most recent)
      expect(weights[1].pounds).toBe(179); // yesterday
      expect(weights[2].pounds).toBe(180); // 2 days ago
    });

    it('should not return other users weights', async () => {
      const user1 = await factory.createUser({ email: 'user1@example.com' });
      const user2 = await factory.createUser({ email: 'user2@example.com' });

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      await weightsService.create(user1.id, {
        date: today.toISOString(),
        pounds: 180,
      });

      await weightsService.create(user2.id, {
        date: today.toISOString(),
        pounds: 200,
      });

      const user1Weights = await weightsService.findAllByUser(user1.id);
      const user2Weights = await weightsService.findAllByUser(user2.id);

      expect(user1Weights.length).toBe(1);
      expect(user1Weights[0].pounds).toBe(180);

      expect(user2Weights.length).toBe(1);
      expect(user2Weights[0].pounds).toBe(200);
    });
  });
});
