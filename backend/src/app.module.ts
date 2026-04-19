import { Module } from '@nestjs/common';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { validationSchema } from './config/validation.schema';
import { getDatabaseConfig } from './config/database.config';
import { AuthModule } from './auth/auth.module';
import { UsersModule } from './users/users.module';
import { WeightsModule } from './weights/weights.module';
import { ReportEntriesModule } from './report-entries/report-entries.module';
import { FoodsModule } from './foods/foods.module';
import { ExercisesModule } from './exercises/exercises.module';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      validationSchema,
      envFilePath: '.env',
    }),
    TypeOrmModule.forRootAsync({
      imports: [ConfigModule],
      inject: [ConfigService],
      useFactory: getDatabaseConfig,
    }),
    AuthModule,
    UsersModule,
    WeightsModule,
    ReportEntriesModule,
    FoodsModule,
    ExercisesModule,
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
