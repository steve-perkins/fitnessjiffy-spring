import {
  IsOptional,
  IsDateString,
  IsEnum,
  IsNumber,
  IsString,
  Min,
  Max,
  Length,
} from 'class-validator';
import { ApiPropertyOptional } from '@nestjs/swagger';
import { Sex } from '../../common/enums/sex.enum';
import { ActivityLevel } from '../../common/enums/activity-level.enum';

export class UpdateProfileDto {
  @ApiPropertyOptional({
    description: 'First name',
    example: 'John',
    minLength: 1,
    maxLength: 20,
  })
  @IsOptional()
  @IsString()
  @Length(1, 20)
  firstName?: string;

  @ApiPropertyOptional({
    description: 'Last name',
    example: 'Doe',
    minLength: 1,
    maxLength: 20,
  })
  @IsOptional()
  @IsString()
  @Length(1, 20)
  lastName?: string;

  @ApiPropertyOptional({
    description: 'Biological sex (for calorie calculations)',
    enum: Sex,
    example: Sex.MALE,
  })
  @IsOptional()
  @IsEnum(Sex)
  sex?: Sex;

  @ApiPropertyOptional({
    description: 'Birthdate (ISO 8601 format)',
    example: '1990-01-01T00:00:00.000Z',
    type: String,
    format: 'date-time',
  })
  @IsOptional()
  @IsDateString()
  birthdate?: string;

  @ApiPropertyOptional({
    description: 'Height in inches',
    example: 70,
    minimum: 24,
    maximum: 108,
  })
  @IsOptional()
  @IsNumber()
  @Min(24)
  @Max(108)
  heightInInches?: number;

  @ApiPropertyOptional({
    description: 'Activity level multiplier for calorie calculations',
    enum: ActivityLevel,
    example: ActivityLevel.MODERATELY_ACTIVE,
  })
  @IsOptional()
  @IsEnum(ActivityLevel)
  activityLevel?: ActivityLevel;

  @ApiPropertyOptional({
    description: 'Timezone (e.g., America/New_York)',
    example: 'America/New_York',
    minLength: 1,
    maxLength: 50,
  })
  @IsOptional()
  @IsString()
  @Length(1, 50)
  timezone?: string;
}
