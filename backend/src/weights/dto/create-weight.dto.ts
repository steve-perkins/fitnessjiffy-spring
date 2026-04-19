import { IsDateString, IsNumber, Min, Max } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class CreateWeightDto {
  @ApiProperty({
    description: 'Date of weight entry (ISO 8601 format)',
    example: '2026-04-18T00:00:00.000Z',
    type: String,
    format: 'date-time',
  })
  @IsDateString()
  date: string;

  @ApiProperty({
    description: 'Weight in pounds',
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
