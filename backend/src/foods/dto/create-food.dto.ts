import {
  IsString,
  IsNumber,
  IsEnum,
  Min,
  Max,
  Length,
} from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';
import { ServingType } from '../../common/enums/serving-type.enum';

export class CreateFoodDto {
  @ApiProperty({
    description: 'Food name',
    example: 'Chicken Breast',
    minLength: 1,
    maxLength: 50,
  })
  @IsString()
  @Length(1, 50)
  name: string;

  @ApiProperty({
    description: 'Default serving type for this food',
    enum: ServingType,
    example: ServingType.OUNCE,
  })
  @IsEnum(ServingType)
  defaultServingType: ServingType;

  @ApiProperty({
    description: 'Quantity of default serving type (e.g., 4 ounces)',
    example: 4,
    minimum: 0,
    maximum: 1000,
  })
  @IsNumber()
  @Min(0)
  @Max(1000)
  servingTypeQty: number;

  @ApiProperty({
    description: 'Calories per serving',
    example: 187,
    minimum: 0,
    maximum: 10000,
  })
  @IsNumber()
  @Min(0)
  @Max(10000)
  calories: number;

  @ApiProperty({
    description: 'Fat in grams per serving',
    example: 4,
    minimum: 0,
    maximum: 1000,
  })
  @IsNumber()
  @Min(0)
  @Max(1000)
  fat: number;

  @ApiProperty({
    description: 'Saturated fat in grams per serving',
    example: 1,
    minimum: 0,
    maximum: 1000,
  })
  @IsNumber()
  @Min(0)
  @Max(1000)
  saturatedFat: number;

  @ApiProperty({
    description: 'Carbohydrates in grams per serving',
    example: 0,
    minimum: 0,
    maximum: 1000,
  })
  @IsNumber()
  @Min(0)
  @Max(1000)
  carbs: number;

  @ApiProperty({
    description: 'Fiber in grams per serving',
    example: 0,
    minimum: 0,
    maximum: 1000,
  })
  @IsNumber()
  @Min(0)
  @Max(1000)
  fiber: number;

  @ApiProperty({
    description: 'Sugar in grams per serving',
    example: 0,
    minimum: 0,
    maximum: 1000,
  })
  @IsNumber()
  @Min(0)
  @Max(1000)
  sugar: number;

  @ApiProperty({
    description: 'Protein in grams per serving',
    example: 35,
    minimum: 0,
    maximum: 1000,
  })
  @IsNumber()
  @Min(0)
  @Max(1000)
  protein: number;

  @ApiProperty({
    description: 'Sodium in milligrams per serving',
    example: 78,
    minimum: 0,
    maximum: 10000,
  })
  @IsNumber()
  @Min(0)
  @Max(10000)
  sodium: number;
}
