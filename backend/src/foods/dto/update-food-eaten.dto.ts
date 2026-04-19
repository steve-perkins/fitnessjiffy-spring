import { IsNumber, IsEnum, Min } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';
import { ServingType } from '../../common/enums/serving-type.enum';

export class UpdateFoodEatenDto {
  @ApiProperty({
    description: 'Updated serving type for this food entry',
    enum: ServingType,
    example: ServingType.CUP,
  })
  @IsEnum(ServingType)
  servingType: ServingType;

  @ApiProperty({
    description: 'Updated quantity of serving type consumed',
    example: 2,
    minimum: 0.1,
  })
  @IsNumber()
  @Min(0.1)
  servingQty: number;
}
