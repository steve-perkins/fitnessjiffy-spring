import { ExercisePerformed } from './exercise-performed.entity';
import { Exercise } from './exercise.entity';

describe('ExercisePerformed Entity', () => {
  describe('getCaloriesBurned - Basic Calculations', () => {
    it('should calculate calories for light activity (MET 3.0)', () => {
      const exercise = new Exercise();
      exercise.metabolicEquivalent = 3.0; // Light activity
      exercise.description = 'Walking, 2 mph, slow pace';

      const exercisePerformed = new ExercisePerformed();
      exercisePerformed.exercise = exercise;
      exercisePerformed.minutes = 30;

      const weightInPounds = 150;
      // weight_kg = 150 / 2.2 = 68.18
      // calories = 3.0 × 3.5 × 68.18 ÷ 200 × 30
      // calories = 10.5 × 68.18 ÷ 200 × 30
      // calories = 715.89 ÷ 200 × 30
      // calories = 3.579 × 30
      // calories = 107.37 → floor = 107
      const calories = exercisePerformed.getCaloriesBurned(weightInPounds);
      expect(calories).toBe(107);
    });

    it('should calculate calories for moderate activity (MET 5.0)', () => {
      const exercise = new Exercise();
      exercise.metabolicEquivalent = 5.0; // Moderate activity
      exercise.description = 'Walking, 4 mph, brisk pace';

      const exercisePerformed = new ExercisePerformed();
      exercisePerformed.exercise = exercise;
      exercisePerformed.minutes = 30;

      const weightInPounds = 150;
      // weight_kg = 150 / 2.2 = 68.18
      // calories = 5.0 × 3.5 × 68.18 ÷ 200 × 30
      // calories = 17.5 × 68.18 ÷ 200 × 30
      // calories = 1193.15 ÷ 200 × 30
      // calories = 5.966 × 30
      // calories = 178.98 → floor = 178
      const calories = exercisePerformed.getCaloriesBurned(weightInPounds);
      expect(calories).toBe(178);
    });

    it('should calculate calories for vigorous activity (MET 8.0)', () => {
      const exercise = new Exercise();
      exercise.metabolicEquivalent = 8.0; // Vigorous activity
      exercise.description = 'Running, 5 mph (12 min mile)';

      const exercisePerformed = new ExercisePerformed();
      exercisePerformed.exercise = exercise;
      exercisePerformed.minutes = 30;

      const weightInPounds = 150;
      // weight_kg = 150 / 2.2 = 68.18
      // calories = 8.0 × 3.5 × 68.18 ÷ 200 × 30
      // calories = 28.0 × 68.18 ÷ 200 × 30
      // calories = 1909.04 ÷ 200 × 30
      // calories = 9.545 × 30
      // calories = 286.35 → floor = 286
      const calories = exercisePerformed.getCaloriesBurned(weightInPounds);
      expect(calories).toBe(286);
    });

    it('should calculate calories for very vigorous activity (MET 12.0)', () => {
      const exercise = new Exercise();
      exercise.metabolicEquivalent = 12.0; // Very vigorous
      exercise.description = 'Running, 8 mph (7.5 min mile)';

      const exercisePerformed = new ExercisePerformed();
      exercisePerformed.exercise = exercise;
      exercisePerformed.minutes = 30;

      const weightInPounds = 150;
      // calories = 12.0 × 3.5 × 68.18 ÷ 200 × 30
      // calories = 429.52 → floor = 429
      const calories = exercisePerformed.getCaloriesBurned(weightInPounds);
      expect(calories).toBe(429);
    });
  });

  describe('getCaloriesBurned - Weight Variations', () => {
    let exercise: Exercise;
    let exercisePerformed: ExercisePerformed;

    beforeEach(() => {
      exercise = new Exercise();
      exercise.metabolicEquivalent = 6.0; // Moderate-vigorous
      exercise.description = 'Jogging, general';

      exercisePerformed = new ExercisePerformed();
      exercisePerformed.exercise = exercise;
      exercisePerformed.minutes = 30;
    });

    it('should calculate calories for 100 lb person', () => {
      // weight_kg = 100 / 2.2 = 45.45
      // calories = 6.0 × 3.5 × 45.45 ÷ 200 × 30
      // calories = 143.18 → floor = 143
      const calories = exercisePerformed.getCaloriesBurned(100);
      expect(calories).toBe(143);
    });

    it('should calculate calories for 150 lb person', () => {
      // weight_kg = 150 / 2.2 = 68.18
      // calories = 6.0 × 3.5 × 68.18 ÷ 200 × 30
      // calories = 214.77 → floor = 214
      const calories = exercisePerformed.getCaloriesBurned(150);
      expect(calories).toBe(214);
    });

    it('should calculate calories for 200 lb person', () => {
      // weight_kg = 200 / 2.2 = 90.91
      // calories = 6.0 × 3.5 × 90.91 ÷ 200 × 30
      // calories = 286.36 → floor = 286
      const calories = exercisePerformed.getCaloriesBurned(200);
      expect(calories).toBe(286);
    });

    it('should calculate calories for 250 lb person', () => {
      // weight_kg = 250 / 2.2 = 113.64
      // calories = 6.0 × 3.5 × 113.64 ÷ 200 × 30
      // calories = 357.95 → floor = 357
      const calories = exercisePerformed.getCaloriesBurned(250);
      expect(calories).toBe(357);
    });

    it('should calculate calories for 300 lb person', () => {
      // weight_kg = 300 / 2.2 = 136.36
      // calories = 6.0 × 3.5 × 136.36 ÷ 200 × 30
      // calories = 429.54 → floor = 429
      const calories = exercisePerformed.getCaloriesBurned(300);
      expect(calories).toBe(429);
    });
  });

  describe('getCaloriesBurned - Duration Variations', () => {
    let exercise: Exercise;
    let exercisePerformed: ExercisePerformed;

    beforeEach(() => {
      exercise = new Exercise();
      exercise.metabolicEquivalent = 6.0;
      exercise.description = 'Cycling, moderate effort';

      exercisePerformed = new ExercisePerformed();
      exercisePerformed.exercise = exercise;
    });

    it('should calculate calories for 1 minute', () => {
      exercisePerformed.minutes = 1;
      const calories = exercisePerformed.getCaloriesBurned(150);
      // calories = 6.0 × 3.5 × 68.18 ÷ 200 × 1
      // calories = 7.159 → floor = 7
      expect(calories).toBe(7);
    });

    it('should calculate calories for 15 minutes', () => {
      exercisePerformed.minutes = 15;
      const calories = exercisePerformed.getCaloriesBurned(150);
      // calories = 6.0 × 3.5 × 68.18 ÷ 200 × 15
      // calories = 107.385 → floor = 107
      expect(calories).toBe(107);
    });

    it('should calculate calories for 30 minutes', () => {
      exercisePerformed.minutes = 30;
      const calories = exercisePerformed.getCaloriesBurned(150);
      // calories = 6.0 × 3.5 × 68.18 ÷ 200 × 30
      // calories = 214.77 → floor = 214
      expect(calories).toBe(214);
    });

    it('should calculate calories for 60 minutes', () => {
      exercisePerformed.minutes = 60;
      const calories = exercisePerformed.getCaloriesBurned(150);
      // calories = 6.0 × 3.5 × 68.18 ÷ 200 × 60
      // calories = 429.54 → floor = 429
      expect(calories).toBe(429);
    });

    it('should calculate calories for 120 minutes', () => {
      exercisePerformed.minutes = 120;
      const calories = exercisePerformed.getCaloriesBurned(150);
      // calories = 6.0 × 3.5 × 68.18 ÷ 200 × 120
      // calories = 859.08 → floor = 859
      expect(calories).toBe(859);
    });
  });

  describe('getCaloriesBurned - Edge Cases', () => {
    it('should return 0 calories for 0 minutes', () => {
      const exercise = new Exercise();
      exercise.metabolicEquivalent = 6.0;

      const exercisePerformed = new ExercisePerformed();
      exercisePerformed.exercise = exercise;
      exercisePerformed.minutes = 0;

      const calories = exercisePerformed.getCaloriesBurned(150);
      expect(calories).toBe(0);
    });

    it('should handle very low MET values', () => {
      const exercise = new Exercise();
      exercise.metabolicEquivalent = 1.0; // Sleeping
      exercise.description = 'Sleeping';

      const exercisePerformed = new ExercisePerformed();
      exercisePerformed.exercise = exercise;
      exercisePerformed.minutes = 480; // 8 hours

      const calories = exercisePerformed.getCaloriesBurned(150);
      // calories = 1.0 × 3.5 × 68.18 ÷ 200 × 480
      // calories = 572.71 → floor = 572
      expect(calories).toBe(572);
    });

    it('should handle very high MET values', () => {
      const exercise = new Exercise();
      exercise.metabolicEquivalent = 18.0; // Very vigorous
      exercise.description = 'Running, 10 mph (6 min mile)';

      const exercisePerformed = new ExercisePerformed();
      exercisePerformed.exercise = exercise;
      exercisePerformed.minutes = 20;

      const calories = exercisePerformed.getCaloriesBurned(150);
      // calories = 18.0 × 3.5 × 68.18 ÷ 200 × 20
      // calories = 429.534 → floor = 429
      expect(calories).toBe(429);
    });

    it('should floor calories (not round)', () => {
      const exercise = new Exercise();
      exercise.metabolicEquivalent = 6.0;

      const exercisePerformed = new ExercisePerformed();
      exercisePerformed.exercise = exercise;
      exercisePerformed.minutes = 25;

      const calories = exercisePerformed.getCaloriesBurned(150);
      // calories = 6.0 × 3.5 × 68.18 ÷ 200 × 25
      // calories = 178.975 → floor = 178 (not 179)
      expect(calories).toBe(178);
    });
  });

  describe('Real-World Examples', () => {
    it('should calculate calories for 30 min moderate walking (150 lb person)', () => {
      const exercise = new Exercise();
      exercise.code = '17190';
      exercise.metabolicEquivalent = 3.5;
      exercise.category = 'walking';
      exercise.description = 'Walking, 3.0 mph, level, moderate pace, firm surface';

      const exercisePerformed = new ExercisePerformed();
      exercisePerformed.exercise = exercise;
      exercisePerformed.minutes = 30;

      const calories = exercisePerformed.getCaloriesBurned(150);
      // calories = 3.5 × 3.5 × 68.18 ÷ 200 × 30
      // calories = 125.42 → floor = 125
      expect(calories).toBe(125);
    });

    it('should calculate calories for 45 min cycling (180 lb person)', () => {
      const exercise = new Exercise();
      exercise.code = '01010';
      exercise.metabolicEquivalent = 7.5;
      exercise.category = 'bicycling';
      exercise.description = 'Bicycling, 12-13.9 mph, moderate';

      const exercisePerformed = new ExercisePerformed();
      exercisePerformed.exercise = exercise;
      exercisePerformed.minutes = 45;

      const calories = exercisePerformed.getCaloriesBurned(180);
      // weight_kg = 180 / 2.2 = 81.82
      // calories = 7.5 × 3.5 × 81.82 ÷ 200 × 45
      // calories = 483.75 → floor = 483
      expect(calories).toBe(483);
    });

    it('should calculate calories for 60 min swimming (200 lb person)', () => {
      const exercise = new Exercise();
      exercise.code = '18310';
      exercise.metabolicEquivalent = 9.8;
      exercise.category = 'swimming';
      exercise.description = 'Swimming, freestyle, vigorous effort';

      const exercisePerformed = new ExercisePerformed();
      exercisePerformed.exercise = exercise;
      exercisePerformed.minutes = 60;

      const calories = exercisePerformed.getCaloriesBurned(200);
      // weight_kg = 200 / 2.2 = 90.91
      // calories = 9.8 × 3.5 × 90.91 ÷ 200 × 60
      // calories = 935.70 → floor = 935
      expect(calories).toBe(935);
    });

    it('should calculate calories for 20 min HIIT workout (160 lb person)', () => {
      const exercise = new Exercise();
      exercise.code = '02065';
      exercise.metabolicEquivalent = 12.3;
      exercise.category = 'conditioning exercise';
      exercise.description = 'Calisthenics, vigorous, push-ups, sit-ups';

      const exercisePerformed = new ExercisePerformed();
      exercisePerformed.exercise = exercise;
      exercisePerformed.minutes = 20;

      const calories = exercisePerformed.getCaloriesBurned(160);
      // weight_kg = 160 / 2.2 = 72.73
      // calories = 12.3 × 3.5 × 72.73 ÷ 200 × 20
      // calories = 313.27 → floor = 313
      expect(calories).toBe(313);
    });

    it('should calculate calories for 90 min yoga (130 lb person)', () => {
      const exercise = new Exercise();
      exercise.code = '02101';
      exercise.metabolicEquivalent = 2.5;
      exercise.category = 'conditioning exercise';
      exercise.description = 'Yoga, Hatha';

      const exercisePerformed = new ExercisePerformed();
      exercisePerformed.exercise = exercise;
      exercisePerformed.minutes = 90;

      const calories = exercisePerformed.getCaloriesBurned(130);
      // weight_kg = 130 / 2.2 = 59.09
      // calories = 2.5 × 3.5 × 59.09 ÷ 200 × 90
      // calories = 232.69 → floor = 232
      expect(calories).toBe(232);
    });
  });

  describe('Formula Verification', () => {
    it('should use correct formula: MET × 3.5 × weight_kg ÷ 200 × minutes', () => {
      const exercise = new Exercise();
      exercise.metabolicEquivalent = 5.0;

      const exercisePerformed = new ExercisePerformed();
      exercisePerformed.exercise = exercise;
      exercisePerformed.minutes = 40;

      const weightInPounds = 176; // 80 kg
      const weightInKilograms = weightInPounds / 2.2; // 80

      // Manual calculation
      const expected = Math.floor(
        (5.0 * 3.5 * weightInKilograms * 40) / 200,
      );

      const actual = exercisePerformed.getCaloriesBurned(weightInPounds);
      expect(actual).toBe(expected);
    });
  });
});
