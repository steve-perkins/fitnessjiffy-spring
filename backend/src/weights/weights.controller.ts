import {
  Controller,
  Get,
  Post,
  Patch,
  Delete,
  Body,
  Param,
  Query,
  UseGuards,
  HttpCode,
  HttpStatus,
  ParseUUIDPipe,
} from '@nestjs/common';
import {
  ApiTags,
  ApiBearerAuth,
  ApiOperation,
  ApiResponse,
  ApiQuery,
  ApiParam,
} from '@nestjs/swagger';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { User } from '../entities/user.entity';
import { WeightsService } from './weights.service';
import { CreateWeightDto } from './dto/create-weight.dto';
import { UpdateWeightDto } from './dto/update-weight.dto';

@ApiTags('weights')
@ApiBearerAuth('JWT-auth')
@Controller('weights')
@UseGuards(JwtAuthGuard)
export class WeightsController {
  constructor(private readonly weightsService: WeightsService) {}

  @ApiOperation({ summary: 'Get all weight entries for user' })
  @ApiResponse({
    status: 200,
    description: 'Array of weight entries sorted by date DESC',
  })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @Get()
  async findAll(@CurrentUser() user: User) {
    const weights = await this.weightsService.findAllByUser(user.id);
    return weights.map((weight) => ({
      id: weight.id,
      date: weight.date,
      pounds: weight.pounds,
    }));
  }

  @ApiOperation({
    summary: 'Get most recent weight on or before specific date',
    description:
      'Returns the most recent weight entry on or before the specified date (gap-filling logic)',
  })
  @ApiQuery({
    name: 'date',
    required: true,
    description: 'Date to query (ISO 8601 format)',
    type: String,
    example: '2026-04-18T00:00:00.000Z',
  })
  @ApiResponse({
    status: 200,
    description: 'Weight entry found or null if no weight exists',
  })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @Get('on-date')
  async findMostRecentOnDate(
    @CurrentUser() user: User,
    @Query('date') dateString: string,
  ) {
    const date = new Date(dateString);
    const weight = await this.weightsService.findMostRecentOnDate(
      user.id,
      date,
    );

    if (!weight) {
      return null;
    }

    return {
      id: weight.id,
      date: weight.date,
      pounds: weight.pounds,
    };
  }

  @ApiOperation({ summary: 'Create new weight entry' })
  @ApiResponse({ status: 201, description: 'Weight entry created successfully' })
  @ApiResponse({ status: 400, description: 'Invalid input data' })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @ApiResponse({
    status: 409,
    description: 'Weight entry already exists for this date',
  })
  @Post()
  @HttpCode(HttpStatus.CREATED)
  async create(
    @CurrentUser() user: User,
    @Body() createWeightDto: CreateWeightDto,
  ) {
    const weight = await this.weightsService.create(user.id, createWeightDto);
    return {
      id: weight.id,
      date: weight.date,
      pounds: weight.pounds,
    };
  }

  @ApiOperation({ summary: 'Update weight entry' })
  @ApiParam({
    name: 'id',
    description: 'Weight entry UUID',
    type: String,
    format: 'uuid',
  })
  @ApiResponse({ status: 200, description: 'Weight entry updated successfully' })
  @ApiResponse({ status: 400, description: 'Invalid input data' })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @ApiResponse({ status: 404, description: 'Weight entry not found' })
  @Patch(':id')
  async update(
    @Param('id', ParseUUIDPipe) id: string,
    @CurrentUser() user: User,
    @Body() updateWeightDto: UpdateWeightDto,
  ) {
    const weight = await this.weightsService.update(
      id,
      user.id,
      updateWeightDto,
    );
    return {
      id: weight.id,
      date: weight.date,
      pounds: weight.pounds,
    };
  }

  @ApiOperation({ summary: 'Delete weight entry' })
  @ApiParam({
    name: 'id',
    description: 'Weight entry UUID',
    type: String,
    format: 'uuid',
  })
  @ApiResponse({ status: 200, description: 'Weight entry deleted successfully' })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @ApiResponse({ status: 404, description: 'Weight entry not found' })
  @Delete(':id')
  @HttpCode(HttpStatus.OK)
  async delete(
    @Param('id', ParseUUIDPipe) id: string,
    @CurrentUser() user: User,
  ) {
    await this.weightsService.delete(id, user.id);
    return { message: 'Weight deleted successfully' };
  }
}
