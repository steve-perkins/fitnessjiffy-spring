import { IsNumber, Min, Max } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class UpdateExercisePerformedDto {
  @ApiProperty({
    description: 'Updated duration in minutes (max 1440 = 24 hours)',
    example: 45,
    minimum: 1,
    maximum: 1440,
  })
  @IsNumber()
  @Min(1)
  @Max(1440)
  minutes: number;
}
