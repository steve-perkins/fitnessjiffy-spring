import { Controller, Get, Query, UseGuards } from '@nestjs/common';
import {
  ApiTags,
  ApiBearerAuth,
  ApiOperation,
  ApiResponse,
  ApiQuery,
} from '@nestjs/swagger';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { User } from '../entities/user.entity';
import { ReportEntriesService } from './report-entries.service';

@ApiTags('reports')
@ApiBearerAuth('JWT-auth')
@Controller('report-entries')
@UseGuards(JwtAuthGuard)
export class ReportEntriesController {
  constructor(private readonly reportEntriesService: ReportEntriesService) {}

  @ApiOperation({
    summary: 'Get daily report entries',
    description:
      'Returns daily report entries with weight and net calories. If startDate and endDate are provided, filters to that range. If omitted, returns all report entries for the user.',
  })
  @ApiQuery({
    name: 'startDate',
    required: false,
    description: 'Report start date (ISO 8601 format) - optional',
    type: String,
    example: '2026-04-11T00:00:00.000Z',
  })
  @ApiQuery({
    name: 'endDate',
    required: false,
    description: 'Report end date (ISO 8601 format) - optional',
    type: String,
    example: '2026-04-18T00:00:00.000Z',
  })
  @ApiResponse({
    status: 200,
    description: 'Array of daily report entries (all or filtered by date range)',
  })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @Get()
  async findByDateRange(
    @CurrentUser() user: User,
    @Query('startDate') startDateString?: string,
    @Query('endDate') endDateString?: string,
  ) {
    let startDate: Date | undefined;
    let endDate: Date | undefined;

    if (startDateString && endDateString) {
      startDate = new Date(startDateString);
      endDate = new Date(endDateString);
    }

    const reportEntries =
      await this.reportEntriesService.findByUserAndDateRange(
        user.id,
        startDate,
        endDate,
      );

    return reportEntries.map((entry) => ({
      id: entry.id,
      date: entry.date,
      pounds: entry.pounds,
      netCalories: entry.netCalories,
    }));
  }
}
