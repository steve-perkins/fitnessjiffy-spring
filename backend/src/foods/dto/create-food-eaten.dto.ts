import {
  IsDateString,
  IsUUID,
  IsNumber,
  IsEnum,
  Min,
} from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';
import { ServingType } from '../../common/enums/serving-type.enum';

export class CreateFoodEatenDto {
  @ApiProperty({
    description: 'UUID of the food item',
    example: '123e4567-e89b-12d3-a456-426614174000',
    format: 'uuid',
  })
  @IsUUID()
  foodId: string;

  @ApiProperty({
    description: 'Date when food was eaten (ISO 8601 format)',
    example: '2026-04-18T00:00:00.000Z',
    type: String,
    format: 'date-time',
  })
  @IsDateString()
  date: string;

  @ApiProperty({
    description: 'Serving type for this food entry',
    enum: ServingType,
    example: ServingType.OUNCE,
  })
  @IsEnum(ServingType)
  servingType: ServingType;

  @ApiProperty({
    description: 'Quantity of serving type consumed',
    example: 4,
    minimum: 0.1,
  })
  @IsNumber()
  @Min(0.1)
  servingQty: number;
}
