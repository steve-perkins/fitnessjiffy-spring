import { ApiProperty } from '@nestjs/swagger';
import { IsEmail } from 'class-validator';

export class DevTokenDto {
  @ApiProperty({
    description: 'User email address to generate token for',
    example: 'user@example.com',
    type: String,
  })
  @IsEmail()
  email: string;
}
