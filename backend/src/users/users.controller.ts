import { Controller, Get, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { User } from '../entities/user.entity';

@Controller('users')
export class UsersController {
  @Get('me')
  @UseGuards(JwtAuthGuard)
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
}
