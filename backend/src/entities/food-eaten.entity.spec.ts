import { FoodEaten } from './food-eaten.entity';
import { Food } from './food.entity';
import { ServingType } from '../common/enums/serving-type.enum';

describe('FoodEaten Entity', () => {
  describe('getRatio - Same Serving Type', () => {
    it('should calculate ratio when using same serving type with 1:1 ratio', () => {
      const food = new Food();
      food.defaultServingType = ServingType.CUP;
      food.servingTypeQty = 1.0;
      food.calories = 100;

      const foodEaten = new FoodEaten();
      foodEaten.food = food;
      foodEaten.servingType = ServingType.CUP;
      foodEaten.servingQty = 1.0;

      // Access private method via bracket notation for testing
      const ratio = foodEaten['getRatio']();
      expect(ratio).toBe(1.0);
    });

    it('should calculate ratio when using same serving type with 2x quantity', () => {
      const food = new Food();
      food.defaultServingType = ServingType.CUP;
      food.servingTypeQty = 1.0;
      food.calories = 100;

      const foodEaten = new FoodEaten();
      foodEaten.food = food;
      foodEaten.servingType = ServingType.CUP;
      foodEaten.servingQty = 2.0;

      const ratio = foodEaten['getRatio']();
      expect(ratio).toBe(2.0);
    });

    it('should calculate ratio when using same serving type with fractional quantity', () => {
      const food = new Food();
      food.defaultServingType = ServingType.CUP;
      food.servingTypeQty = 1.0;
      food.calories = 100;

      const foodEaten = new FoodEaten();
      foodEaten.food = food;
      foodEaten.servingType = ServingType.CUP;
      foodEaten.servingQty = 0.5;

      const ratio = foodEaten['getRatio']();
      expect(ratio).toBe(0.5);
    });

    it('should calculate ratio when default serving has quantity > 1', () => {
      const food = new Food();
      food.defaultServingType = ServingType.OUNCE;
      food.servingTypeQty = 4.0; // 4 ounces per serving
      food.calories = 165;

      const foodEaten = new FoodEaten();
      foodEaten.food = food;
      foodEaten.servingType = ServingType.OUNCE;
      foodEaten.servingQty = 8.0; // 8 ounces eaten

      const ratio = foodEaten['getRatio']();
      expect(ratio).toBe(2.0); // 8 / 4 = 2x
    });
  });

  describe('getRatio - Different Serving Types (Ounce Conversions)', () => {
    it('should convert CUP to OUNCE correctly', () => {
      const food = new Food();
      food.defaultServingType = ServingType.CUP; // 8 oz
      food.servingTypeQty = 1.0;
      food.calories = 100;

      const foodEaten = new FoodEaten();
      foodEaten.food = food;
      foodEaten.servingType = ServingType.OUNCE; // 1 oz
      foodEaten.servingQty = 16.0; // 16 oz eaten

      // 16 oz eaten / 8 oz per cup = 2 cups
      const ratio = foodEaten['getRatio']();
      expect(ratio).toBe(2.0);
    });

    it('should convert OUNCE to CUP correctly', () => {
      const food = new Food();
      food.defaultServingType = ServingType.OUNCE; // 1 oz
      food.servingTypeQty = 4.0; // 4 oz per serving
      food.calories = 100;

      const foodEaten = new FoodEaten();
      foodEaten.food = food;
      foodEaten.servingType = ServingType.CUP; // 8 oz
      foodEaten.servingQty = 1.0; // 1 cup eaten (8 oz)

      // 8 oz eaten / 4 oz default = 2x
      const ratio = foodEaten['getRatio']();
      expect(ratio).toBe(2.0);
    });

    it('should convert TABLESPOON to CUP correctly', () => {
      const food = new Food();
      food.defaultServingType = ServingType.CUP; // 8 oz
      food.servingTypeQty = 1.0;
      food.calories = 100;

      const foodEaten = new FoodEaten();
      foodEaten.food = food;
      foodEaten.servingType = ServingType.TABLESPOON; // 0.5 oz
      foodEaten.servingQty = 16.0; // 16 tablespoons eaten

      // 16 tablespoons * 0.5 oz = 8 oz eaten / 8 oz per cup = 1 cup
      const ratio = foodEaten['getRatio']();
      expect(ratio).toBe(1.0);
    });

    it('should convert TEASPOON to TABLESPOON correctly', () => {
      const food = new Food();
      food.defaultServingType = ServingType.TABLESPOON; // 0.5 oz
      food.servingTypeQty = 2.0; // 2 tablespoons per serving
      food.calories = 50;

      const foodEaten = new FoodEaten();
      foodEaten.food = food;
      foodEaten.servingType = ServingType.TEASPOON; // 0.1667 oz
      foodEaten.servingQty = 6.0; // 6 teaspoons eaten

      // 6 teaspoons * 0.1667 oz = 1.0002 oz
      // 2 tablespoons * 0.5 oz = 1.0 oz
      // ratio = 1.0002 / 1.0 ≈ 1.0
      const ratio = foodEaten['getRatio']();
      expect(ratio).toBeCloseTo(1.0, 2);
    });

    it('should convert POUND to OUNCE correctly', () => {
      const food = new Food();
      food.defaultServingType = ServingType.OUNCE; // 1 oz
      food.servingTypeQty = 4.0; // 4 oz per serving
      food.calories = 100;

      const foodEaten = new FoodEaten();
      foodEaten.food = food;
      foodEaten.servingType = ServingType.POUND; // 16 oz
      foodEaten.servingQty = 0.5; // 0.5 pounds eaten

      // 0.5 pounds * 16 oz = 8 oz eaten / 4 oz default = 2x
      const ratio = foodEaten['getRatio']();
      expect(ratio).toBe(2.0);
    });

    it('should convert GRAM to OUNCE correctly', () => {
      const food = new Food();
      food.defaultServingType = ServingType.OUNCE; // 1 oz
      food.servingTypeQty = 1.0; // 1 oz per serving
      food.calories = 100;

      const foodEaten = new FoodEaten();
      foodEaten.food = food;
      foodEaten.servingType = ServingType.GRAM; // 0.03527 oz
      foodEaten.servingQty = 100.0; // 100 grams eaten

      // 100 grams * 0.03527 oz = 3.527 oz eaten / 1 oz default = 3.527x
      const ratio = foodEaten['getRatio']();
      expect(ratio).toBeCloseTo(3.527, 2);
    });
  });

  describe('getRatio - Edge Cases', () => {
    it('should return 0 when denominator is 0', () => {
      const food = new Food();
      food.defaultServingType = ServingType.CUP;
      food.servingTypeQty = 0.0; // Invalid but should be handled
      food.calories = 100;

      const foodEaten = new FoodEaten();
      foodEaten.food = food;
      foodEaten.servingType = ServingType.OUNCE;
      foodEaten.servingQty = 1.0;

      const ratio = foodEaten['getRatio']();
      expect(ratio).toBe(0);
    });

    it('should handle CUSTOM serving type (0 ounces)', () => {
      const food = new Food();
      food.defaultServingType = ServingType.CUSTOM;
      food.servingTypeQty = 1.0;
      food.calories = 100;

      const foodEaten = new FoodEaten();
      foodEaten.food = food;
      foodEaten.servingType = ServingType.CUSTOM;
      foodEaten.servingQty = 2.0;

      const ratio = foodEaten['getRatio']();
      expect(ratio).toBe(2.0); // Same type, so simple ratio
    });

    it('should return 0 when servingQty is 0', () => {
      const food = new Food();
      food.defaultServingType = ServingType.CUP;
      food.servingTypeQty = 1.0;
      food.calories = 100;

      const foodEaten = new FoodEaten();
      foodEaten.food = food;
      foodEaten.servingType = ServingType.CUP;
      foodEaten.servingQty = 0.0;

      const ratio = foodEaten['getRatio']();
      expect(ratio).toBe(0);
    });
  });

  describe('getCalories', () => {
    it('should calculate calories correctly with Math.floor', () => {
      const food = new Food();
      food.defaultServingType = ServingType.CUP;
      food.servingTypeQty = 1.0;
      food.calories = 100;

      const foodEaten = new FoodEaten();
      foodEaten.food = food;
      foodEaten.servingType = ServingType.CUP;
      foodEaten.servingQty = 2.5;

      // 100 * 2.5 = 250
      expect(foodEaten.getCalories()).toBe(250);
    });

    it('should floor calories (not round)', () => {
      const food = new Food();
      food.defaultServingType = ServingType.CUP;
      food.servingTypeQty = 1.0;
      food.calories = 100;

      const foodEaten = new FoodEaten();
      foodEaten.food = food;
      foodEaten.servingType = ServingType.CUP;
      foodEaten.servingQty = 1.99;

      // 100 * 1.99 = 199 (not 200)
      expect(foodEaten.getCalories()).toBe(199);
    });

    it('should calculate calories with serving type conversion', () => {
      const food = new Food();
      food.defaultServingType = ServingType.CUP; // 8 oz
      food.servingTypeQty = 1.0;
      food.calories = 216; // Brown rice example

      const foodEaten = new FoodEaten();
      foodEaten.food = food;
      foodEaten.servingType = ServingType.OUNCE; // 1 oz
      foodEaten.servingQty = 4.0; // 4 oz = 0.5 cups

      // 4 oz / 8 oz per cup = 0.5x
      // 216 * 0.5 = 108
      expect(foodEaten.getCalories()).toBe(108);
    });
  });

  describe('Nutritional Getters', () => {
    let food: Food;
    let foodEaten: FoodEaten;

    beforeEach(() => {
      food = new Food();
      food.defaultServingType = ServingType.OUNCE;
      food.servingTypeQty = 4.0; // 4 oz per serving
      food.calories = 165;
      food.fat = 3.6;
      food.saturatedFat = 1.0;
      food.carbs = 0;
      food.fiber = 0;
      food.sugar = 0;
      food.protein = 31;
      food.sodium = 74;

      foodEaten = new FoodEaten();
      foodEaten.food = food;
      foodEaten.servingType = ServingType.OUNCE;
      foodEaten.servingQty = 8.0; // 2x serving
    });

    it('should calculate fat correctly', () => {
      expect(foodEaten.getFat()).toBe(7.2); // 3.6 * 2
    });

    it('should calculate saturated fat correctly', () => {
      expect(foodEaten.getSaturatedFat()).toBe(2.0); // 1.0 * 2
    });

    it('should calculate carbs correctly', () => {
      expect(foodEaten.getCarbs()).toBe(0); // 0 * 2
    });

    it('should calculate fiber correctly', () => {
      expect(foodEaten.getFiber()).toBe(0); // 0 * 2
    });

    it('should calculate sugar correctly', () => {
      expect(foodEaten.getSugar()).toBe(0); // 0 * 2
    });

    it('should calculate protein correctly', () => {
      expect(foodEaten.getProtein()).toBe(62); // 31 * 2
    });

    it('should calculate sodium correctly', () => {
      expect(foodEaten.getSodium()).toBe(148); // 74 * 2
    });
  });

  describe('Real-World Example - Chicken Breast', () => {
    it('should calculate nutritional values for 8 oz chicken breast', () => {
      const chickenBreast = new Food();
      chickenBreast.name = 'Chicken Breast';
      chickenBreast.defaultServingType = ServingType.OUNCE;
      chickenBreast.servingTypeQty = 4; // 4 oz serving
      chickenBreast.calories = 165;
      chickenBreast.fat = 3.6;
      chickenBreast.saturatedFat = 1.0;
      chickenBreast.carbs = 0;
      chickenBreast.fiber = 0;
      chickenBreast.sugar = 0;
      chickenBreast.protein = 31;
      chickenBreast.sodium = 74;

      const foodEaten = new FoodEaten();
      foodEaten.food = chickenBreast;
      foodEaten.servingType = ServingType.OUNCE;
      foodEaten.servingQty = 8; // 8 oz eaten (2x serving)

      expect(foodEaten.getCalories()).toBe(330); // 165 * 2
      expect(foodEaten.getProtein()).toBe(62); // 31 * 2
      expect(foodEaten.getFat()).toBe(7.2); // 3.6 * 2
      expect(foodEaten.getSaturatedFat()).toBe(2.0); // 1.0 * 2
      expect(foodEaten.getSodium()).toBe(148); // 74 * 2
    });
  });

  describe('Real-World Example - Brown Rice', () => {
    it('should calculate nutritional values for 0.5 cups brown rice', () => {
      const brownRice = new Food();
      brownRice.name = 'Brown Rice';
      brownRice.defaultServingType = ServingType.CUP;
      brownRice.servingTypeQty = 1; // 1 cup serving
      brownRice.calories = 216;
      brownRice.fat = 1.8;
      brownRice.saturatedFat = 0.4;
      brownRice.carbs = 45;
      brownRice.fiber = 3.5;
      brownRice.sugar = 0.7;
      brownRice.protein = 5;
      brownRice.sodium = 10;

      const foodEaten = new FoodEaten();
      foodEaten.food = brownRice;
      foodEaten.servingType = ServingType.CUP;
      foodEaten.servingQty = 0.5; // 0.5 cups eaten

      expect(foodEaten.getCalories()).toBe(108); // 216 * 0.5
      expect(foodEaten.getCarbs()).toBe(22.5); // 45 * 0.5
      expect(foodEaten.getProtein()).toBe(2.5); // 5 * 0.5
      expect(foodEaten.getFiber()).toBe(1.75); // 3.5 * 0.5
    });
  });

  describe('Real-World Example - Tablespoons of Olive Oil', () => {
    it('should calculate calories for 2 tablespoons of olive oil', () => {
      const oliveOil = new Food();
      oliveOil.name = 'Olive Oil';
      oliveOil.defaultServingType = ServingType.TABLESPOON;
      oliveOil.servingTypeQty = 1; // 1 tablespoon serving
      oliveOil.calories = 120;
      oliveOil.fat = 14;
      oliveOil.saturatedFat = 2;
      oliveOil.carbs = 0;
      oliveOil.fiber = 0;
      oliveOil.sugar = 0;
      oliveOil.protein = 0;
      oliveOil.sodium = 0;

      const foodEaten = new FoodEaten();
      foodEaten.food = oliveOil;
      foodEaten.servingType = ServingType.TABLESPOON;
      foodEaten.servingQty = 2; // 2 tablespoons

      expect(foodEaten.getCalories()).toBe(240); // 120 * 2
      expect(foodEaten.getFat()).toBe(28); // 14 * 2
      expect(foodEaten.getSaturatedFat()).toBe(4); // 2 * 2
    });
  });
});
