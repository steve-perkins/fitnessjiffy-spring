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
import { FoodsService } from './foods.service';
import { CreateFoodDto } from './dto/create-food.dto';
import { UpdateFoodDto } from './dto/update-food.dto';
import { CreateFoodEatenDto } from './dto/create-food-eaten.dto';
import { UpdateFoodEatenDto } from './dto/update-food-eaten.dto';

@ApiTags('foods')
@ApiBearerAuth('JWT-auth')
@Controller('foods')
@UseGuards(JwtAuthGuard)
export class FoodsController {
  constructor(private readonly foodsService: FoodsService) {}

  @ApiOperation({
    summary: 'Get all foods visible to user',
    description: 'Returns global foods and user-owned custom foods',
  })
  @ApiResponse({
    status: 200,
    description: 'Array of foods (global + user-owned)',
  })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @Get()
  async findAll(@CurrentUser() user: User) {
    const foods = await this.foodsService.findVisibleByUser(user.id);
    return foods.map((food) => ({
      id: food.id,
      name: food.name,
      defaultServingType: food.defaultServingType,
      servingTypeQty: food.servingTypeQty,
      calories: food.calories,
      fat: food.fat,
      saturatedFat: food.saturatedFat,
      carbs: food.carbs,
      fiber: food.fiber,
      sugar: food.sugar,
      protein: food.protein,
      sodium: food.sodium,
      ownerId: food.owner?.id || null,
    }));
  }

  @ApiOperation({
    summary: 'Search foods with fuzzy matching',
    description: 'Searches global and user-owned foods by name',
  })
  @ApiQuery({
    name: 'q',
    required: true,
    description: 'Search query string',
    type: String,
    example: 'chicken',
  })
  @ApiResponse({ status: 200, description: 'Array of matching foods' })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @Get('search')
  async search(@CurrentUser() user: User, @Query('q') query: string) {
    if (!query || query.trim().length === 0) {
      return [];
    }

    const foods = await this.foodsService.searchFoods(user.id, query);
    return foods.map((food) => ({
      id: food.id,
      name: food.name,
      defaultServingType: food.defaultServingType,
      servingTypeQty: food.servingTypeQty,
      calories: food.calories,
      fat: food.fat,
      saturatedFat: food.saturatedFat,
      carbs: food.carbs,
      fiber: food.fiber,
      sugar: food.sugar,
      protein: food.protein,
      sodium: food.sodium,
      ownerId: food.owner?.id || null,
    }));
  }

  @ApiOperation({ summary: 'Create custom food' })
  @ApiResponse({ status: 201, description: 'Custom food created successfully' })
  @ApiResponse({ status: 400, description: 'Invalid input data' })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @ApiResponse({
    status: 409,
    description: 'Food with this name already exists',
  })
  @Post()
  @HttpCode(HttpStatus.CREATED)
  async create(
    @CurrentUser() user: User,
    @Body() createFoodDto: CreateFoodDto,
  ) {
    const food = await this.foodsService.createFood(user.id, createFoodDto);
    return {
      id: food.id,
      name: food.name,
      defaultServingType: food.defaultServingType,
      servingTypeQty: food.servingTypeQty,
      calories: food.calories,
      fat: food.fat,
      saturatedFat: food.saturatedFat,
      carbs: food.carbs,
      fiber: food.fiber,
      sugar: food.sugar,
      protein: food.protein,
      sodium: food.sodium,
      ownerId: food.owner?.id || null,
    };
  }

  @ApiOperation({ summary: 'Update custom food' })
  @ApiParam({
    name: 'id',
    description: 'Food UUID',
    type: String,
    format: 'uuid',
  })
  @ApiResponse({ status: 200, description: 'Food updated successfully' })
  @ApiResponse({ status: 400, description: 'Invalid input data' })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @ApiResponse({ status: 404, description: 'Food not found or not owned by user' })
  @ApiResponse({
    status: 409,
    description: 'Food with new name already exists',
  })
  @Patch(':id')
  async update(
    @Param('id', ParseUUIDPipe) id: string,
    @CurrentUser() user: User,
    @Body() updateFoodDto: UpdateFoodDto,
  ) {
    const food = await this.foodsService.updateFood(id, user.id, updateFoodDto);
    return {
      id: food.id,
      name: food.name,
      defaultServingType: food.defaultServingType,
      servingTypeQty: food.servingTypeQty,
      calories: food.calories,
      fat: food.fat,
      saturatedFat: food.saturatedFat,
      carbs: food.carbs,
      fiber: food.fiber,
      sugar: food.sugar,
      protein: food.protein,
      sodium: food.sodium,
      ownerId: food.owner?.id || null,
    };
  }

  @ApiOperation({ summary: 'Delete custom food' })
  @ApiParam({
    name: 'id',
    description: 'Food UUID',
    type: String,
    format: 'uuid',
  })
  @ApiResponse({ status: 204, description: 'Food deleted successfully' })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @ApiResponse({ status: 404, description: 'Food not found or not owned by user' })
  @Delete(':id')
  @HttpCode(HttpStatus.NO_CONTENT)
  async delete(
    @Param('id', ParseUUIDPipe) id: string,
    @CurrentUser() user: User,
  ) {
    await this.foodsService.deleteFood(id, user.id);
  }

  // FoodEaten endpoints

  @ApiOperation({
    summary: 'Get foods eaten on specific date or date range',
    description:
      'Returns foods eaten for a single date or date range. Requires either "date" or both "startDate" and "endDate".',
  })
  @ApiQuery({
    name: 'date',
    required: false,
    description: 'Get foods eaten on specific date (ISO 8601 format)',
    type: String,
    example: '2026-04-18T00:00:00.000Z',
  })
  @ApiQuery({
    name: 'startDate',
    required: false,
    description: 'Get foods eaten from this date',
    type: String,
  })
  @ApiQuery({
    name: 'endDate',
    required: false,
    description: 'Get foods eaten until this date',
    type: String,
  })
  @ApiResponse({
    status: 200,
    description: 'Array of foods eaten with calculated calories',
  })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @Get('eaten')
  async findEaten(
    @CurrentUser() user: User,
    @Query('date') dateString?: string,
    @Query('startDate') startDateString?: string,
    @Query('endDate') endDateString?: string,
  ) {
    let foodsEaten;

    if (dateString) {
      // Single date query
      const date = new Date(dateString);
      foodsEaten = await this.foodsService.findEatenOnDate(user.id, date);
    } else if (startDateString && endDateString) {
      // Date range query
      const startDate = new Date(startDateString);
      const endDate = new Date(endDateString);
      foodsEaten = await this.foodsService.findEatenInRange(
        user.id,
        startDate,
        endDate,
      );
    } else {
      return [];
    }

    return foodsEaten.map((foodEaten) => ({
      id: foodEaten.id,
      date: foodEaten.date,
      servingType: foodEaten.servingType,
      servingQty: foodEaten.servingQty,
      calories: foodEaten.getCalories(),
      food: {
        id: foodEaten.food.id,
        name: foodEaten.food.name,
        defaultServingType: foodEaten.food.defaultServingType,
        servingTypeQty: foodEaten.food.servingTypeQty,
        calories: foodEaten.food.calories,
        fat: foodEaten.food.fat,
        saturatedFat: foodEaten.food.saturatedFat,
        carbs: foodEaten.food.carbs,
        fiber: foodEaten.food.fiber,
        sugar: foodEaten.food.sugar,
        protein: foodEaten.food.protein,
        sodium: foodEaten.food.sodium,
      },
    }));
  }

  @ApiOperation({ summary: 'Add food to food diary' })
  @ApiResponse({
    status: 201,
    description: 'Food diary entry created successfully',
  })
  @ApiResponse({ status: 400, description: 'Invalid input data' })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @ApiResponse({ status: 404, description: 'Food not found' })
  @Post('eaten')
  @HttpCode(HttpStatus.CREATED)
  async addEaten(
    @CurrentUser() user: User,
    @Body() createFoodEatenDto: CreateFoodEatenDto,
  ) {
    const foodEaten = await this.foodsService.addFoodEaten(
      user.id,
      createFoodEatenDto,
    );
    return {
      id: foodEaten.id,
      date: foodEaten.date,
      servingType: foodEaten.servingType,
      servingQty: foodEaten.servingQty,
      calories: foodEaten.getCalories(),
    };
  }

  @ApiOperation({ summary: 'Update food diary entry' })
  @ApiParam({
    name: 'id',
    description: 'Food diary entry UUID',
    type: String,
    format: 'uuid',
  })
  @ApiResponse({
    status: 200,
    description: 'Food diary entry updated successfully',
  })
  @ApiResponse({ status: 400, description: 'Invalid input data' })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @ApiResponse({ status: 404, description: 'Food diary entry not found' })
  @Patch('eaten/:id')
  async updateEaten(
    @Param('id', ParseUUIDPipe) id: string,
    @CurrentUser() user: User,
    @Body() updateFoodEatenDto: UpdateFoodEatenDto,
  ) {
    const foodEaten = await this.foodsService.updateFoodEaten(
      id,
      user.id,
      updateFoodEatenDto,
    );
    return {
      id: foodEaten.id,
      date: foodEaten.date,
      servingType: foodEaten.servingType,
      servingQty: foodEaten.servingQty,
      calories: foodEaten.getCalories(),
    };
  }

  @ApiOperation({ summary: 'Delete food diary entry' })
  @ApiParam({
    name: 'id',
    description: 'Food diary entry UUID',
    type: String,
    format: 'uuid',
  })
  @ApiResponse({
    status: 204,
    description: 'Food diary entry deleted successfully',
  })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @ApiResponse({ status: 404, description: 'Food diary entry not found' })
  @Delete('eaten/:id')
  @HttpCode(HttpStatus.NO_CONTENT)
  async deleteEaten(
    @Param('id', ParseUUIDPipe) id: string,
    @CurrentUser() user: User,
  ) {
    await this.foodsService.deleteFoodEaten(id, user.id);
  }
}
