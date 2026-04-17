/**
 * Serving Type enum with ounce conversion values
 * These values represent how many ounces are in each serving type
 * Used for serving size conversion calculations in FoodEaten entity
 */
export enum ServingType {
  OUNCE = 1,
  CUP = 8,
  POUND = 16,
  PINT = 16,
  TABLESPOON = 0.5,
  TEASPOON = 0.1667,
  GRAM = 0.03527,
  CUSTOM = 0,
}
