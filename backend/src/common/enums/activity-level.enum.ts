/**
 * Activity Level enum with numeric values
 * These values are stored as floats in the database
 * and used for calorie calculation formulas
 */
export enum ActivityLevel {
  SEDENTARY = 1.25,
  LIGHTLY_ACTIVE = 1.3,
  MODERATELY_ACTIVE = 1.5,
  VERY_ACTIVE = 1.7,
  EXTREMELY_ACTIVE = 2.0,
}
