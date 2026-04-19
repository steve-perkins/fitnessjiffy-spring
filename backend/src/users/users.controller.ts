import {
  Controller,
  Get,
  Patch,
  Body,
  UseGuards,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import { ApiTags, ApiBearerAuth, ApiOperation, ApiResponse } from '@nestjs/swagger';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { User } from '../entities/user.entity';
import { UsersService } from './users.service';
import { UpdateProfileDto } from './dto/update-profile.dto';

@ApiTags('users')
@ApiBearerAuth('JWT-auth')
@Controller('users')
@UseGuards(JwtAuthGuard)
export class UsersController {
  constructor(private readonly usersService: UsersService) {}

  @ApiOperation({ summary: 'Get current authenticated user profile' })
  @ApiResponse({ status: 200, description: 'User profile returned' })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @Get('me')
  getCurrentUser(@CurrentUser() user: User) {
    return {
      id: user.id,
      email: user.email,
      firstName: user.firstName,
      lastName: user.lastName,
      sex: user.sex,
      birthdate: user.birthdate,
      heightInInches: user.heightInInches,
      activityLevel: user.activityLevel,
      timezone: user.timezone,
      createdTime: user.createdTime,
      lastUpdatedTime: user.lastUpdatedTime,
    };
  }

  @ApiOperation({ summary: 'Get user profile (alias for /users/me)' })
  @ApiResponse({ status: 200, description: 'User profile returned' })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @Get('profile')
  getProfile(@CurrentUser() user: User) {
    return {
      id: user.id,
      email: user.email,
      firstName: user.firstName,
      lastName: user.lastName,
      sex: user.sex,
      birthdate: user.birthdate,
      heightInInches: user.heightInInches,
      activityLevel: user.activityLevel,
      timezone: user.timezone,
      createdTime: user.createdTime,
      lastUpdatedTime: user.lastUpdatedTime,
    };
  }

  @ApiOperation({ summary: 'Update user profile' })
  @ApiResponse({ status: 200, description: 'User profile updated successfully' })
  @ApiResponse({ status: 400, description: 'Invalid input data' })
  @ApiResponse({ status: 401, description: 'Invalid or missing JWT token' })
  @Patch('profile')
  @HttpCode(HttpStatus.OK)
  async updateProfile(
    @CurrentUser() user: User,
    @Body() updateProfileDto: UpdateProfileDto,
  ) {
    const updatedUser = await this.usersService.updateProfile(
      user.id,
      updateProfileDto,
    );
    return {
      id: updatedUser.id,
      email: updatedUser.email,
      firstName: updatedUser.firstName,
      lastName: updatedUser.lastName,
      sex: updatedUser.sex,
      birthdate: updatedUser.birthdate,
      heightInInches: updatedUser.heightInInches,
      activityLevel: updatedUser.activityLevel,
      timezone: updatedUser.timezone,
      createdTime: updatedUser.createdTime,
      lastUpdatedTime: updatedUser.lastUpdatedTime,
    };
  }
}
