// Enums matching backend
export enum Sex {
  MALE = 'MALE',
  FEMALE = 'FEMALE',
}

export enum ActivityLevel {
  SEDENTARY = 1.25,
  LIGHTLY_ACTIVE = 1.3,
  MODERATELY_ACTIVE = 1.5,
  VERY_ACTIVE = 1.7,
  EXTREMELY_ACTIVE = 2.0,
}

export enum ServingType {
  OUNCE = 'OUNCE',
  CUP = 'CUP',
  POUND = 'POUND',
  PINT = 'PINT',
  TABLESPOON = 'TABLESPOON',
  TEASPOON = 'TEASPOON',
  GRAM = 'GRAM',
  CUSTOM = 'CUSTOM',
}

// User types
export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  sex: Sex | null;
  birthdate: string | null;
  heightInInches: number | null;
  activityLevel: number | null;
  timezone: string | null;
  createdTime: string;
  lastUpdatedTime: string;
}

export interface UpdateProfileDto {
  firstName?: string;
  lastName?: string;
  sex?: Sex;
  birthdate?: string;
  heightInInches?: number;
  activityLevel?: number;
  timezone?: string;
}

// Weight types
export interface Weight {
  id: string;
  date: string;
  pounds: number;
}

export interface CreateWeightDto {
  date: string;
  pounds: number;
}

export interface UpdateWeightDto {
  pounds: number;
}

// Food types
export interface Food {
  id: string;
  name: string;
  defaultServingType: ServingType;
  servingTypeQty: number;
  calories: number;
  fat: number;
  saturatedFat: number;
  carbs: number;
  fiber: number;
  sugar: number;
  protein: number;
  sodium: number;
  ownerId: string | null;
}

export interface CreateFoodDto {
  name: string;
  defaultServingType: ServingType;
  servingTypeQty: number;
  calories: number;
  fat: number;
  saturatedFat: number;
  carbs: number;
  fiber: number;
  sugar: number;
  protein: number;
  sodium: number;
}

export interface UpdateFoodDto extends Partial<CreateFoodDto> {}

export interface FoodEaten {
  id: string;
  date: string;
  servingType: ServingType;
  servingQty: number;
  food: Food;
}

export interface CreateFoodEatenDto {
  foodId: string;
  date: string;
  servingType: ServingType;
  servingQty: number;
}

export interface UpdateFoodEatenDto {
  servingType?: ServingType;
  servingQty?: number;
}

// Exercise types
export interface Exercise {
  id: string;
  category: string;
  description: string;
  metabolicEquivalent: number;
}

export interface ExercisePerformed {
  id: string;
  date: string;
  minutes: number;
  exercise: Exercise;
}

export interface CreateExercisePerformedDto {
  exerciseId: string;
  date: string;
  minutes: number;
}

export interface UpdateExercisePerformedDto {
  minutes: number;
}

// Report types
export interface ReportEntry {
  id: string;
  date: string;
  pounds: number;
  netCalories: number;
}

// Auth types
export interface AuthResponse {
  access_token: string;
  user: {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
  };
}

// Serving type display helpers
export const SERVING_TYPE_LABELS: Record<ServingType, string> = {
  [ServingType.OUNCE]: 'oz',
  [ServingType.CUP]: 'cup',
  [ServingType.POUND]: 'lb',
  [ServingType.PINT]: 'pint',
  [ServingType.TABLESPOON]: 'tbsp',
  [ServingType.TEASPOON]: 'tsp',
  [ServingType.GRAM]: 'g',
  [ServingType.CUSTOM]: 'serving',
};

export const ACTIVITY_LEVEL_LABELS: Record<number, string> = {
  1.25: 'Sedentary (little or no exercise)',
  1.3: 'Lightly Active (1-3 days/week)',
  1.5: 'Moderately Active (3-5 days/week)',
  1.7: 'Very Active (6-7 days/week)',
  2.0: 'Extremely Active (twice per day)',
};

// Serving type conversion ratios (to ounces)
export const SERVING_TYPE_RATIOS: Record<ServingType, number> = {
  [ServingType.OUNCE]: 1,
  [ServingType.CUP]: 8,
  [ServingType.POUND]: 16,
  [ServingType.PINT]: 16,
  [ServingType.TABLESPOON]: 0.5,
  [ServingType.TEASPOON]: 0.166667,
  [ServingType.GRAM]: 0.035274,
  [ServingType.CUSTOM]: 0,
};