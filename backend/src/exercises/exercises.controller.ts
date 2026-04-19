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
import { ExercisesService } from './exercises.service';
import { CreateExercisePerformedDto } from './dto/create-exercise-performed.dto';
import { UpdateExercisePerformedDto } from './dto/update-exercise-performed.dto';

@ApiTags('exercises')
@ApiBearerAuth('JWT-auth')
@Controller('exercises')
@UseGuards(JwtAuthGuard)
export class ExercisesController {
  constructor(private readonly exercisesService: ExercisesService) {}

  @ApiOperation({ summary: 'Get all exercise categories' })
  @ApiResponse({
    status: 200,
    description: 'Array of distinct exercise categories',
  })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @Get('categories')
  async getCategories() {
    return this.exercisesService.findAllCategories();
  }

  @ApiOperation({ summary: 'Get exercises by category' })
  @ApiQuery({
    name: 'category',
    required: true,
    description: 'Exercise category name',
    type: String,
    example: 'running',
  })
  @ApiResponse({
    status: 200,
    description: 'Array of exercises in the specified category',
  })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @Get('by-category')
  async findByCategory(@Query('category') category: string) {
    const exercises =
      await this.exercisesService.findByCategory(category);
    return exercises.map((exercise) => ({
      id: exercise.id,
      category: exercise.category,
      description: exercise.description,
      metabolicEquivalent: exercise.metabolicEquivalent,
    }));
  }

  @ApiOperation({
    summary: 'Search exercises with fuzzy matching',
    description: 'Searches global exercises by description',
  })
  @ApiQuery({
    name: 'q',
    required: true,
    description: 'Search query string',
    type: String,
    example: 'running',
  })
  @ApiResponse({ status: 200, description: 'Array of matching exercises' })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @Get('search')
  async search(@Query('q') query: string) {
    if (!query || query.trim().length === 0) {
      return [];
    }

    const exercises = await this.exercisesService.searchExercises(query);
    return exercises.map((exercise) => ({
      id: exercise.id,
      category: exercise.category,
      description: exercise.description,
      metabolicEquivalent: exercise.metabolicEquivalent,
    }));
  }

  // ExercisePerformed endpoints

  @ApiOperation({
    summary: 'Get exercises performed on specific date or date range',
    description:
      'Returns exercises performed for a single date or date range. Requires either "date" or both "startDate" and "endDate".',
  })
  @ApiQuery({
    name: 'date',
    required: false,
    description: 'Get exercises performed on specific date (ISO 8601 format)',
    type: String,
    example: '2026-04-18T00:00:00.000Z',
  })
  @ApiQuery({
    name: 'startDate',
    required: false,
    description: 'Get exercises performed from this date',
    type: String,
  })
  @ApiQuery({
    name: 'endDate',
    required: false,
    description: 'Get exercises performed until this date',
    type: String,
  })
  @ApiResponse({
    status: 200,
    description: 'Array of exercises performed',
  })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @Get('performed')
  async findPerformed(
    @CurrentUser() user: User,
    @Query('date') dateString?: string,
    @Query('startDate') startDateString?: string,
    @Query('endDate') endDateString?: string,
  ) {
    let exercisesPerformed;

    if (dateString) {
      // Single date query
      const date = new Date(dateString);
      exercisesPerformed = await this.exercisesService.findPerformedOnDate(
        user.id,
        date,
      );
    } else if (startDateString && endDateString) {
      // Date range query
      const startDate = new Date(startDateString);
      const endDate = new Date(endDateString);
      exercisesPerformed = await this.exercisesService.findPerformedInRange(
        user.id,
        startDate,
        endDate,
      );
    } else {
      return [];
    }

    return exercisesPerformed.map((exercisePerformed) => ({
      id: exercisePerformed.id,
      date: exercisePerformed.date,
      minutes: exercisePerformed.minutes,
      exercise: {
        id: exercisePerformed.exercise.id,
        category: exercisePerformed.exercise.category,
        description: exercisePerformed.exercise.description,
        metabolicEquivalent: exercisePerformed.exercise.metabolicEquivalent,
      },
    }));
  }

  @ApiOperation({ summary: 'Add exercise to exercise diary' })
  @ApiResponse({
    status: 201,
    description: 'Exercise diary entry created successfully',
  })
  @ApiResponse({ status: 400, description: 'Invalid input data' })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @ApiResponse({ status: 404, description: 'Exercise not found' })
  @Post('performed')
  @HttpCode(HttpStatus.CREATED)
  async addPerformed(
    @CurrentUser() user: User,
    @Body() createExercisePerformedDto: CreateExercisePerformedDto,
  ) {
    const exercisePerformed =
      await this.exercisesService.addExercisePerformed(
        user.id,
        createExercisePerformedDto,
      );
    return {
      id: exercisePerformed.id,
      date: exercisePerformed.date,
      minutes: exercisePerformed.minutes,
    };
  }

  @ApiOperation({ summary: 'Update exercise diary entry' })
  @ApiParam({
    name: 'id',
    description: 'Exercise diary entry UUID',
    type: String,
    format: 'uuid',
  })
  @ApiResponse({
    status: 200,
    description: 'Exercise diary entry updated successfully',
  })
  @ApiResponse({ status: 400, description: 'Invalid input data' })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @ApiResponse({ status: 404, description: 'Exercise diary entry not found' })
  @Patch('performed/:id')
  async updatePerformed(
    @Param('id', ParseUUIDPipe) id: string,
    @CurrentUser() user: User,
    @Body() updateExercisePerformedDto: UpdateExercisePerformedDto,
  ) {
    const exercisePerformed =
      await this.exercisesService.updateExercisePerformed(
        id,
        user.id,
        updateExercisePerformedDto,
      );
    return {
      id: exercisePerformed.id,
      date: exercisePerformed.date,
      minutes: exercisePerformed.minutes,
    };
  }

  @ApiOperation({ summary: 'Delete exercise diary entry' })
  @ApiParam({
    name: 'id',
    description: 'Exercise diary entry UUID',
    type: String,
    format: 'uuid',
  })
  @ApiResponse({
    status: 204,
    description: 'Exercise diary entry deleted successfully',
  })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @ApiResponse({ status: 404, description: 'Exercise diary entry not found' })
  @Delete('performed/:id')
  @HttpCode(HttpStatus.NO_CONTENT)
  async deletePerformed(
    @Param('id', ParseUUIDPipe) id: string,
    @CurrentUser() user: User,
  ) {
    await this.exercisesService.deleteExercisePerformed(id, user.id);
  }
}
