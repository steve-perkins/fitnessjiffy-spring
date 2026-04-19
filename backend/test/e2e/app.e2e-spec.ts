import { Test, TestingModule } from '@nestjs/testing';
import { INestApplication, ValidationPipe } from '@nestjs/common';
import request from 'supertest';
import { DataSource } from 'typeorm';
import { AppModule } from '../../src/app.module';
import { TestDataFactory } from '../integration/helpers/test-data.factory';
import {
  createTestDataSource,
  destroyTestDataSource,
} from '../integration/helpers/test-database.helper';

describe('App E2E Tests', () => {
  let app: INestApplication;
  let dataSource: DataSource;
  let factory: TestDataFactory;
  let jwtToken: string;
  let userId: string;

  beforeAll(async () => {
    // Create test database
    dataSource = await createTestDataSource();
    factory = new TestDataFactory(dataSource);

    // Create test module with our test database
    const moduleFixture: TestingModule = await Test.createTestingModule({
      imports: [AppModule],
    })
      .overrideProvider(DataSource)
      .useValue(dataSource)
      .compile();

    app = moduleFixture.createNestApplication();

    // Apply same validation pipe as main app
    app.useGlobalPipes(
      new ValidationPipe({
        whitelist: true,
        forbidNonWhitelisted: true,
        transform: true,
      }),
    );

    await app.init();

    // Create test user
    const user = await factory.createUser({
      email: 'test@example.com',
    });
    userId = user.id;
  });

  afterAll(async () => {
    await app.close();
    await destroyTestDataSource(dataSource);
  });

  afterEach(async () => {
    // Clean up data between tests (except user)
    await dataSource
      .createQueryBuilder()
      .delete()
      .from('report_entries')
      .execute();
    await dataSource
      .createQueryBuilder()
      .delete()
      .from('exercises_performed')
      .execute();
    await dataSource.createQueryBuilder().delete().from('foods_eaten').execute();
    await dataSource.createQueryBuilder().delete().from('weights').execute();
  });

  describe('Authentication', () => {
    it('should generate JWT with dev-token endpoint', async () => {
      const response = await request(app.getHttpServer())
        .post('/auth/dev-token')
        .send({ email: 'test@example.com' })
        .expect(200);

      expect(response.body.access_token).toBeDefined();
      expect(response.body.user).toBeDefined();
      expect(response.body.user.email).toBe('test@example.com');

      // Store token for subsequent tests
      jwtToken = response.body.access_token;
    });

    it('should reject dev-token with invalid email', async () => {
      await request(app.getHttpServer())
        .post('/auth/dev-token')
        .send({ email: 'nonexistent@example.com' })
        .expect(401);
    });

    it('should access protected endpoint with valid JWT', async () => {
      // First get a token
      const authResponse = await request(app.getHttpServer())
        .post('/auth/dev-token')
        .send({ email: 'test@example.com' })
        .expect(200);

      const token = authResponse.body.access_token;

      // Use token to access protected endpoint
      const response = await request(app.getHttpServer())
        .get('/users/me')
        .set('Authorization', `Bearer ${token}`)
        .expect(200);

      expect(response.body.email).toBe('test@example.com');
    });

    it('should reject protected endpoint without JWT', async () => {
      await request(app.getHttpServer()).get('/users/me').expect(401);
    });

    it('should reject protected endpoint with invalid JWT', async () => {
      await request(app.getHttpServer())
        .get('/users/me')
        .set('Authorization', 'Bearer invalid-token-here')
        .expect(401);
    });
  });

  describe('Weights CRUD Workflow', () => {
    beforeEach(async () => {
      // Get fresh token before each test
      const authResponse = await request(app.getHttpServer())
        .post('/auth/dev-token')
        .send({ email: 'test@example.com' })
        .expect(200);

      jwtToken = authResponse.body.access_token;
    });

    it('should create, read, update, and delete weight', async () => {
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      // CREATE
      const createResponse = await request(app.getHttpServer())
        .post('/weights')
        .set('Authorization', `Bearer ${jwtToken}`)
        .send({
          date: today.toISOString(),
          pounds: 180,
        })
        .expect(201);

      expect(createResponse.body.pounds).toBe(180);
      const weightId = createResponse.body.id;

      // READ (list all)
      const readResponse = await request(app.getHttpServer())
        .get('/weights')
        .set('Authorization', `Bearer ${jwtToken}`)
        .expect(200);

      expect(readResponse.body.length).toBe(1);
      expect(readResponse.body[0].pounds).toBe(180);

      // UPDATE
      const updateResponse = await request(app.getHttpServer())
        .patch(`/weights/${weightId}`)
        .set('Authorization', `Bearer ${jwtToken}`)
        .send({
          pounds: 175,
        })
        .expect(200);

      expect(updateResponse.body.pounds).toBe(175);

      // DELETE
      await request(app.getHttpServer())
        .delete(`/weights/${weightId}`)
        .set('Authorization', `Bearer ${jwtToken}`)
        .expect(200);

      // Verify deletion
      const afterDeleteResponse = await request(app.getHttpServer())
        .get('/weights')
        .set('Authorization', `Bearer ${jwtToken}`)
        .expect(200);

      expect(afterDeleteResponse.body.length).toBe(0);
    });
  });

  describe('Foods CRUD Workflow', () => {
    beforeEach(async () => {
      const authResponse = await request(app.getHttpServer())
        .post('/auth/dev-token')
        .send({ email: 'test@example.com' })
        .expect(200);

      jwtToken = authResponse.body.access_token;
    });

    it('should create custom food and add to food log', async () => {
      // CREATE custom food
      const createFoodResponse = await request(app.getHttpServer())
        .post('/foods')
        .set('Authorization', `Bearer ${jwtToken}`)
        .send({
          name: 'Custom Protein Shake',
          defaultServingType: 1, // OUNCE
          servingTypeQty: 16,
          calories: 200,
          fat: 3,
          saturatedFat: 1,
          carbs: 10,
          fiber: 2,
          sugar: 5,
          protein: 30,
          sodium: 100,
        })
        .expect(201);

      expect(createFoodResponse.body.name).toBe('Custom Protein Shake');
      const foodId = createFoodResponse.body.id;

      // ADD food eaten
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      const addFoodEatenResponse = await request(app.getHttpServer())
        .post('/foods/eaten')
        .set('Authorization', `Bearer ${jwtToken}`)
        .send({
          foodId,
          date: today.toISOString(),
          servingType: 1, // OUNCE
          servingQty: 16,
        })
        .expect(201);

      expect(addFoodEatenResponse.body.servingQty).toBe(16);

      // VERIFY food eaten list
      const listResponse = await request(app.getHttpServer())
        .get(`/foods/eaten?date=${today.toISOString()}`)
        .set('Authorization', `Bearer ${jwtToken}`)
        .expect(200);

      expect(listResponse.body.length).toBe(1);
      expect(listResponse.body[0].food.name).toBe('Custom Protein Shake');
    });
  });

  describe('Exercises Workflow', () => {
    beforeEach(async () => {
      const authResponse = await request(app.getHttpServer())
        .post('/auth/dev-token')
        .send({ email: 'test@example.com' })
        .expect(200);

      jwtToken = authResponse.body.access_token;

      // Create weight (required for exercise calorie calculations)
      await factory.createWeightHistory(
        { id: userId } as any,
        1,
        180,
      );
    });

    it('should add exercise performed', async () => {
      // Create global exercises
      const exercises = await factory.createGlobalExercises();
      const walkingExercise = exercises.find((e) => e.metabolicEquivalent === 3.5);

      const today = new Date();
      today.setHours(0, 0, 0, 0);

      // ADD exercise performed
      const addResponse = await request(app.getHttpServer())
        .post('/exercises/performed')
        .set('Authorization', `Bearer ${jwtToken}`)
        .send({
          exerciseId: walkingExercise!.id,
          date: today.toISOString(),
          minutes: 30,
        })
        .expect(201);

      expect(addResponse.body.minutes).toBe(30);

      // VERIFY exercise performed list
      const listResponse = await request(app.getHttpServer())
        .get(`/exercises/performed?date=${today.toISOString()}`)
        .set('Authorization', `Bearer ${jwtToken}`)
        .expect(200);

      expect(listResponse.body.length).toBe(1);
      expect(listResponse.body[0].minutes).toBe(30);
    });

    it('should search exercises with fuzzy matching', async () => {
      // Create global exercises
      await factory.createGlobalExercises();

      // Search for "running"
      const searchResponse = await request(app.getHttpServer())
        .get('/exercises/search?q=running')
        .set('Authorization', `Bearer ${jwtToken}`)
        .expect(200);

      expect(searchResponse.body.length).toBeGreaterThan(0);
      expect(
        searchResponse.body.some((exercise: any) =>
          exercise.description.toLowerCase().includes('running'),
        ),
      ).toBe(true);
    });
  });

  describe('Report Entries - End-to-End Calculation', () => {
    beforeEach(async () => {
      const authResponse = await request(app.getHttpServer())
        .post('/auth/dev-token')
        .send({ email: 'test@example.com' })
        .expect(200);

      jwtToken = authResponse.body.access_token;
    });

    it('should calculate report entries from foods, exercises, and weights', async () => {
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      // 1. Create weight
      await request(app.getHttpServer())
        .post('/weights')
        .set('Authorization', `Bearer ${jwtToken}`)
        .send({
          date: today.toISOString(),
          pounds: 180,
        })
        .expect(201);

      // 2. Add food eaten (500 calories)
      const foods = await factory.createGlobalFoods();
      const food = foods[0];

      await request(app.getHttpServer())
        .post('/foods/eaten')
        .set('Authorization', `Bearer ${jwtToken}`)
        .send({
          foodId: food.id,
          date: today.toISOString(),
          servingType: food.defaultServingType,
          servingQty: 1,
        })
        .expect(201);

      // 3. Add exercise performed (burns ~125 calories)
      const exercises = await factory.createGlobalExercises();
      const walkingExercise = exercises.find((e) => e.metabolicEquivalent === 3.5);

      await request(app.getHttpServer())
        .post('/exercises/performed')
        .set('Authorization', `Bearer ${jwtToken}`)
        .send({
          exerciseId: walkingExercise!.id,
          date: today.toISOString(),
          minutes: 30,
        })
        .expect(201);

      // 4. Verify report entries calculated automatically
      const reportResponse = await request(app.getHttpServer())
        .get(
          `/report-entries?startDate=${today.toISOString()}&endDate=${today.toISOString()}`,
        )
        .set('Authorization', `Bearer ${jwtToken}`)
        .expect(200);

      // Should have at least one report entry
      expect(reportResponse.body.length).toBeGreaterThan(0);

      // Get the most recent report (should be today's)
      const sortedReports = reportResponse.body.sort((a: any, b: any) => {
        return new Date(b.date).getTime() - new Date(a.date).getTime();
      });

      const mostRecentReport = sortedReports[0];

      expect(mostRecentReport).toBeDefined();
      expect(mostRecentReport.pounds).toBe(180);
      // Net calories should be defined (can be positive or negative depending on food vs exercise)
      expect(mostRecentReport.netCalories).toBeDefined();
      expect(typeof mostRecentReport.netCalories).toBe('number');
    });
  });

  describe('User Profile', () => {
    beforeEach(async () => {
      const authResponse = await request(app.getHttpServer())
        .post('/auth/dev-token')
        .send({ email: 'test@example.com' })
        .expect(200);

      jwtToken = authResponse.body.access_token;
    });

    it('should get user profile', async () => {
      const response = await request(app.getHttpServer())
        .get('/users/me')
        .set('Authorization', `Bearer ${jwtToken}`)
        .expect(200);

      expect(response.body.email).toBe('test@example.com');
      expect(response.body.firstName).toBeDefined();
      expect(response.body.lastName).toBeDefined();
    });

    it('should update user profile', async () => {
      const response = await request(app.getHttpServer())
        .patch('/users/profile')
        .set('Authorization', `Bearer ${jwtToken}`)
        .send({
          firstName: 'Updated',
          lastName: 'Name',
          heightInInches: 72,
        })
        .expect(200);

      expect(response.body.firstName).toBe('Updated');
      expect(response.body.lastName).toBe('Name');
      expect(response.body.heightInInches).toBe(72);
    });
  });
});
