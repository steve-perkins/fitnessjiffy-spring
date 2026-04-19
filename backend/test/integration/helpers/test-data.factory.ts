import { DataSource } from 'typeorm';
import { User } from '../../../src/entities/user.entity';
import { Weight } from '../../../src/entities/weight.entity';
import { Food } from '../../../src/entities/food.entity';
import { FoodEaten } from '../../../src/entities/food-eaten.entity';
import { Exercise } from '../../../src/entities/exercise.entity';
import { ExercisePerformed } from '../../../src/entities/exercise-performed.entity';
import { Sex } from '../../../src/common/enums/sex.enum';
import { ActivityLevel } from '../../../src/common/enums/activity-level.enum';
import { ServingType } from '../../../src/common/enums/serving-type.enum';

/**
 * Factory for creating dynamic test data relative to current date.
 * This avoids the performance issues caused by fixed-date test data
 * getting further and further into the past.
 */
export class TestDataFactory {
  constructor(private dataSource: DataSource) {}

  /**
   * Create a test user with basic profile information
   */
  async createUser(overrides?: Partial<User>): Promise<User> {
    const userRepo = this.dataSource.getRepository(User);

    const user = userRepo.create({
      email: `test${Date.now()}@example.com`,
      firstName: 'Test',
      lastName: 'User',
      sex: Sex.MALE,
      birthdate: new Date('1990-01-01'),
      heightInInches: 70,
      activityLevel: ActivityLevel.MODERATELY_ACTIVE,
      timezone: 'America/New_York',
      ...overrides,
    });

    return await userRepo.save(user);
  }

  /**
   * Create weights for a user going back N days from today.
   * Simulates realistic weight fluctuation with sine wave.
   *
   * @param user - User entity
   * @param daysBack - Number of days of weight history to create
   * @param startWeight - Starting weight (default 180 lbs)
   */
  async createWeightHistory(
    user: User,
    daysBack: number = 30,
    startWeight: number = 180,
  ): Promise<Weight[]> {
    const weightRepo = this.dataSource.getRepository(Weight);
    const weights: Weight[] = [];

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    for (let i = 0; i < daysBack; i++) {
      const date = new Date(today);
      date.setDate(date.getDate() - i);

      // Simulate realistic weight fluctuation (±5 lbs over a week)
      const fluctuation = Math.sin(i / 7) * 5;
      const pounds = startWeight + fluctuation - (i * 0.1); // Slow weight loss trend

      const weight = weightRepo.create({
        user,
        date,
        pounds: Math.round(pounds * 10) / 10, // Round to 1 decimal
      });

      weights.push(await weightRepo.save(weight));
    }

    return weights;
  }

  /**
   * Create global foods (foods without an owner)
   * These are visible to all users
   */
  async createGlobalFoods(): Promise<Food[]> {
    const foodRepo = this.dataSource.getRepository(Food);

    const foodsData = [
      {
        name: 'Chicken Breast',
        defaultServingType: ServingType.OUNCE,
        servingTypeQty: 4,
        calories: 165,
        fat: 3.6,
        saturatedFat: 1.0,
        carbs: 0,
        fiber: 0,
        sugar: 0,
        protein: 31,
        sodium: 74,
      },
      {
        name: 'Brown Rice',
        defaultServingType: ServingType.CUP,
        servingTypeQty: 1,
        calories: 216,
        fat: 1.8,
        saturatedFat: 0.4,
        carbs: 45,
        fiber: 3.5,
        sugar: 0.7,
        protein: 5,
        sodium: 10,
      },
      {
        name: 'Broccoli',
        defaultServingType: ServingType.CUP,
        servingTypeQty: 1,
        calories: 55,
        fat: 0.6,
        saturatedFat: 0.1,
        carbs: 11,
        fiber: 2.4,
        sugar: 2.2,
        protein: 3.7,
        sodium: 30,
      },
      {
        name: 'Salmon',
        defaultServingType: ServingType.OUNCE,
        servingTypeQty: 4,
        calories: 206,
        fat: 12,
        saturatedFat: 2.5,
        carbs: 0,
        fiber: 0,
        sugar: 0,
        protein: 23,
        sodium: 50,
      },
      {
        name: 'Olive Oil',
        defaultServingType: ServingType.TABLESPOON,
        servingTypeQty: 1,
        calories: 120,
        fat: 14,
        saturatedFat: 2,
        carbs: 0,
        fiber: 0,
        sugar: 0,
        protein: 0,
        sodium: 0,
      },
    ];

    const foods: Food[] = [];
    for (const foodData of foodsData) {
      const food = foodRepo.create({ ...foodData, owner: null });
      foods.push(await foodRepo.save(food));
    }

    return foods;
  }

  /**
   * Create custom food owned by a user
   */
  async createCustomFood(user: User, overrides?: Partial<Food>): Promise<Food> {
    const foodRepo = this.dataSource.getRepository(Food);

    const food = foodRepo.create({
      name: `Custom Food ${Date.now()}`,
      defaultServingType: ServingType.CUP,
      servingTypeQty: 1,
      calories: 200,
      fat: 5,
      saturatedFat: 1,
      carbs: 30,
      fiber: 2,
      sugar: 5,
      protein: 10,
      sodium: 50,
      owner: user,
      ...overrides,
    });

    return await foodRepo.save(food);
  }

  /**
   * Create foods eaten for a user over recent days.
   * Creates 2-3 meals per day with varying quantities.
   *
   * @param user - User entity
   * @param foods - Array of foods to eat from
   * @param daysBack - Number of days to create food logs for
   */
  async createFoodLogs(
    user: User,
    foods: Food[],
    daysBack: number = 7,
  ): Promise<FoodEaten[]> {
    const foodEatenRepo = this.dataSource.getRepository(FoodEaten);
    const foodsEaten: FoodEaten[] = [];

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    for (let i = 0; i < daysBack; i++) {
      const date = new Date(today);
      date.setDate(date.getDate() - i);

      // Create 2-3 meals per day
      const mealsPerDay = 2 + (i % 2);

      for (let j = 0; j < mealsPerDay; j++) {
        const food = foods[j % foods.length];

        const foodEaten = foodEatenRepo.create({
          user,
          food,
          date,
          servingType: food.defaultServingType,
          servingQty: 1 + (j * 0.5), // Varying quantities: 1, 1.5, 2
        });

        foodsEaten.push(await foodEatenRepo.save(foodEaten));
      }
    }

    return foodsEaten;
  }

  /**
   * Create global exercises (exercises without category restriction)
   */
  async createGlobalExercises(): Promise<Exercise[]> {
    const exerciseRepo = this.dataSource.getRepository(Exercise);

    const exercisesData = [
      {
        code: '17190',
        metabolicEquivalent: 3.5,
        category: 'walking',
        description: 'Walking, 3.0 mph, moderate pace',
      },
      {
        code: '01010',
        metabolicEquivalent: 7.5,
        category: 'bicycling',
        description: 'Bicycling, 12-13.9 mph, moderate effort',
      },
      {
        code: '12030',
        metabolicEquivalent: 8.0,
        category: 'running',
        description: 'Running, 5 mph (12 min mile)',
      },
      {
        code: '18310',
        metabolicEquivalent: 9.8,
        category: 'swimming',
        description: 'Swimming, freestyle, vigorous',
      },
      {
        code: '02065',
        metabolicEquivalent: 12.3,
        category: 'conditioning',
        description: 'Calisthenics, vigorous (push-ups, sit-ups)',
      },
    ];

    const exercises: Exercise[] = [];
    for (const exerciseData of exercisesData) {
      const exercise = exerciseRepo.create(exerciseData);
      exercises.push(await exerciseRepo.save(exercise));
    }

    return exercises;
  }

  /**
   * Create exercise logs for a user over recent days.
   * Creates 1-2 exercises per day with varying durations.
   *
   * @param user - User entity
   * @param exercises - Array of exercises to perform
   * @param daysBack - Number of days to create exercise logs for
   */
  async createExerciseLogs(
    user: User,
    exercises: Exercise[],
    daysBack: number = 7,
  ): Promise<ExercisePerformed[]> {
    const exercisePerformedRepo = this.dataSource.getRepository(ExercisePerformed);
    const exercisesPerformed: ExercisePerformed[] = [];

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    for (let i = 0; i < daysBack; i++) {
      const date = new Date(today);
      date.setDate(date.getDate() - i);

      // Create 0-2 exercises per day (some days have no exercise)
      const exercisesPerDay = i % 3 === 0 ? 0 : 1 + (i % 2);

      for (let j = 0; j < exercisesPerDay; j++) {
        const exercise = exercises[j % exercises.length];

        const exercisePerformed = exercisePerformedRepo.create({
          user,
          exercise,
          date,
          minutes: 30 + (j * 15), // Varying durations: 30, 45 minutes
        });

        exercisesPerformed.push(await exercisePerformedRepo.save(exercisePerformed));
      }
    }

    return exercisesPerformed;
  }

  /**
   * Create a complete user profile with weight history, food logs, and exercise logs.
   * This is useful for testing report generation.
   *
   * @param daysBack - Number of days of history to create
   */
  async createCompleteUserProfile(daysBack: number = 30): Promise<{
    user: User;
    weights: Weight[];
    foods: Food[];
    foodsEaten: FoodEaten[];
    exercises: Exercise[];
    exercisesPerformed: ExercisePerformed[];
  }> {
    const user = await this.createUser();
    const weights = await this.createWeightHistory(user, daysBack);
    const foods = await this.createGlobalFoods();
    const foodsEaten = await this.createFoodLogs(user, foods, daysBack);
    const exercises = await this.createGlobalExercises();
    const exercisesPerformed = await this.createExerciseLogs(user, exercises, daysBack);

    return {
      user,
      weights,
      foods,
      foodsEaten,
      exercises,
      exercisesPerformed,
    };
  }
}
