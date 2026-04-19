import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ReportEntry } from '../entities/report-entry.entity';
import { Weight } from '../entities/weight.entity';
import { FoodEaten } from '../entities/food-eaten.entity';
import { ExercisePerformed } from '../entities/exercise-performed.entity';
import { ReportEntriesController } from './report-entries.controller';
import { ReportEntriesService } from './report-entries.service';

@Module({
  imports: [
    TypeOrmModule.forFeature([
      ReportEntry,
      Weight,
      FoodEaten,
      ExercisePerformed,
    ]),
  ],
  controllers: [ReportEntriesController],
  providers: [ReportEntriesService],
  exports: [ReportEntriesService],
})
export class ReportEntriesModule {}
