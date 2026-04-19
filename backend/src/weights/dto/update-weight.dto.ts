import { IsNumber, Min, Max } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class UpdateWeightDto {
  @ApiProperty({
    description: 'Updated weight in pounds',
    example: 180,
    type: Number,
    minimum: 50,
    maximum: 1000,
  })
  @IsNumber()
  @Min(50)
  @Max(1000)
  pounds: number;
}
