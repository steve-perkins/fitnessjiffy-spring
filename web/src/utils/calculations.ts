import { ServingType, SERVING_TYPE_RATIOS, FoodEaten, Food, ExercisePerformed } from '../types';

/**
 * Calculate the ratio for serving size conversion
 * This mirrors the backend FoodEaten.getRatio() method
 */
export function getServingRatio(
  food: Food,
  servingType: ServingType,
  servingQty: number
): number {
  // For CUSTOM serving type, the ratio equals servingQty divided by servingTypeQty
  if (servingType === ServingType.CUSTOM || food.defaultServingType === ServingType.CUSTOM) {
    return servingQty / food.servingTypeQty;
  }

  // Get the ratio for the eaten serving type vs the food's default serving type
  const eatenRatio = SERVING_TYPE_RATIOS[servingType];
  const defaultRatio = SERVING_TYPE_RATIOS[food.defaultServingType];

  // Calculate total ounces eaten and total ounces in food's serving
  const eatenOunces = eatenRatio * servingQty;
  const defaultOunces = defaultRatio * food.servingTypeQty;

  return eatenOunces / defaultOunces;
}

/**
 * Calculate calories for a food eaten entry
 */
export function calculateFoodCalories(foodEaten: FoodEaten): number {
  const ratio = getServingRatio(foodEaten.food, foodEaten.servingType, foodEaten.servingQty);
  return Math.round(foodEaten.food.calories * ratio);
}

/**
 * Calculate a nutritional value for a food eaten entry
 */
export function calculateNutrition(
  food: Food,
  servingType: ServingType,
  servingQty: number,
  nutrientValue: number
): number {
  const ratio = getServingRatio(food, servingType, servingQty);
  return nutrientValue * ratio;
}

/**
 * Calculate all nutritional values for a food eaten entry
 */
export function calculateAllNutrition(foodEaten: FoodEaten) {
  const ratio = getServingRatio(foodEaten.food, foodEaten.servingType, foodEaten.servingQty);
  const food = foodEaten.food;

  return {
    calories: Math.round((food.calories || 0) * ratio),
    fat: (food.fat || 0) * ratio,
    saturatedFat: (food.saturatedFat || 0) * ratio,
    carbs: (food.carbs || 0) * ratio,
    fiber: (food.fiber || 0) * ratio,
    sugar: (food.sugar || 0) * ratio,
    protein: (food.protein || 0) * ratio,
    sodium: (food.sodium || 0) * ratio,
  };
}

/**
 * Calculate calories burned for an exercise performed
 * Formula: MET * 3.5 * weight_kg / 200 * minutes
 */
export function calculateExerciseCalories(
  exercisePerformed: ExercisePerformed,
  weightPounds: number
): number {
  const weightKg = weightPounds * 0.453592;
  const met = exercisePerformed.exercise.metabolicEquivalent;
  const minutes = exercisePerformed.minutes;

  return Math.round((met * 3.5 * weightKg / 200) * minutes);
}

/**
 * Calculate BMI from weight (pounds) and height (inches)
 */
export function calculateBMI(weightPounds: number, heightInches: number): number {
  if (!heightInches || heightInches === 0) return 0;
  return (weightPounds / (heightInches * heightInches)) * 703;
}

/**
 * Calculate BMR (Basal Metabolic Rate) using Mifflin-St Jeor equation
 */
export function calculateBMR(
  weightPounds: number,
  heightInches: number,
  age: number,
  sex: 'MALE' | 'FEMALE'
): number {
  const weightKg = weightPounds * 0.453592;
  const heightCm = heightInches * 2.54;

  if (sex === 'MALE') {
    return 10 * weightKg + 6.25 * heightCm - 5 * age + 5;
  } else {
    return 10 * weightKg + 6.25 * heightCm - 5 * age - 161;
  }
}

/**
 * Calculate maintenance calories (TDEE) from BMR and activity level
 */
export function calculateMaintenanceCalories(bmr: number, activityLevel: number): number {
  return Math.round(bmr * activityLevel);
}

/**
 * Format a number for display
 */
export function formatNumber(value: number, decimals = 1): string {
  if (Number.isInteger(value)) {
    return value.toString();
  }
  return value.toFixed(decimals);
}

/**
 * Calculate age from birthdate
 */
export function calculateAge(birthdate: string | Date): number {
  const birth = typeof birthdate === 'string' ? new Date(birthdate) : birthdate;
  const today = new Date();
  let age = today.getFullYear() - birth.getFullYear();
  const monthDiff = today.getMonth() - birth.getMonth();

  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
    age--;
  }

  return age;
}

/**
 * Convert height in inches to feet and inches display
 */
export function formatHeight(totalInches: number): string {
  const feet = Math.floor(totalInches / 12);
  const inches = totalInches % 12;
  return `${feet}' ${inches}"`;
}
