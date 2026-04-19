import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Weight } from '../entities/weight.entity';
import { ReportEntriesModule } from '../report-entries/report-entries.module';
import { WeightsController } from './weights.controller';
import { WeightsService } from './weights.service';

@Module({
  imports: [TypeOrmModule.forFeature([Weight]), ReportEntriesModule],
  controllers: [WeightsController],
  providers: [WeightsService],
  exports: [WeightsService],
})
export class WeightsModule {}
