import { Injectable } from '@nestjs/common';

@Injectable()
export class AppService {
  getHello(): string {
    return 'Fitness Tracker API - Phase 2: NestJS Backend Core';
  }
}
