import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Exercise } from '../entities/exercise.entity';
import { ExercisePerformed } from '../entities/exercise-performed.entity';
import { ReportEntriesModule } from '../report-entries/report-entries.module';
import { ExercisesController } from './exercises.controller';
import { ExercisesService } from './exercises.service';

@Module({
  imports: [
    TypeOrmModule.forFeature([Exercise, ExercisePerformed]),
    ReportEntriesModule,
  ],
  controllers: [ExercisesController],
  providers: [ExercisesService],
  exports: [ExercisesService],
})
export class ExercisesModule {}
