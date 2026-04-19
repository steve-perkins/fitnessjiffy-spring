import {
  IsDateString,
  IsUUID,
  IsNumber,
  Min,
  Max,
} from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class CreateExercisePerformedDto {
  @ApiProperty({
    description: 'UUID of the exercise',
    example: '123e4567-e89b-12d3-a456-426614174000',
    format: 'uuid',
  })
  @IsUUID()
  exerciseId: string;

  @ApiProperty({
    description: 'Date when exercise was performed (ISO 8601 format)',
    example: '2026-04-18T00:00:00.000Z',
    type: String,
    format: 'date-time',
  })
  @IsDateString()
  date: string;

  @ApiProperty({
    description: 'Duration in minutes (max 1440 = 24 hours)',
    example: 30,
    minimum: 1,
    maximum: 1440,
  })
  @IsNumber()
  @Min(1)
  @Max(1440)
  minutes: number;
}
