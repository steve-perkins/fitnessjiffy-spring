import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { User } from '../entities/user.entity';
import { UpdateProfileDto } from './dto/update-profile.dto';

@Injectable()
export class UsersService {
  constructor(
    @InjectRepository(User)
    private userRepository: Repository<User>,
  ) {}

  async findById(id: string): Promise<User | null> {
    return this.userRepository.findOne({ where: { id } });
  }

  async findByEmail(email: string): Promise<User | null> {
    return this.userRepository.findOne({ where: { email } });
  }

  async updateProfile(
    userId: string,
    updateProfileDto: UpdateProfileDto,
  ): Promise<User> {
    const user = await this.findById(userId);
    if (!user) {
      throw new NotFoundException(`User with ID ${userId} not found`);
    }

    // Update only provided fields
    if (updateProfileDto.firstName !== undefined) {
      user.firstName = updateProfileDto.firstName;
    }
    if (updateProfileDto.lastName !== undefined) {
      user.lastName = updateProfileDto.lastName;
    }
    if (updateProfileDto.sex !== undefined) {
      user.sex = updateProfileDto.sex;
    }
    if (updateProfileDto.birthdate !== undefined) {
      user.birthdate = new Date(updateProfileDto.birthdate);
    }
    if (updateProfileDto.heightInInches !== undefined) {
      user.heightInInches = updateProfileDto.heightInInches;
    }
    if (updateProfileDto.activityLevel !== undefined) {
      user.activityLevel = updateProfileDto.activityLevel;
    }
    if (updateProfileDto.timezone !== undefined) {
      user.timezone = updateProfileDto.timezone;
    }

    return this.userRepository.save(user);
  }
}
