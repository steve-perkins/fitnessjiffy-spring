import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Food } from '../entities/food.entity';
import { FoodEaten } from '../entities/food-eaten.entity';
import { ReportEntriesModule } from '../report-entries/report-entries.module';
import { FoodsController } from './foods.controller';
import { FoodsService } from './foods.service';

@Module({
  imports: [
    TypeOrmModule.forFeature([Food, FoodEaten]),
    ReportEntriesModule,
  ],
  controllers: [FoodsController],
  providers: [FoodsService],
  exports: [FoodsService],
})
export class FoodsModule {}
