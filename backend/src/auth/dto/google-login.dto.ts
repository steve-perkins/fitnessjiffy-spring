import { IsString, IsNotEmpty } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class GoogleLoginDto {
  @ApiProperty({
    description: 'Google OAuth ID token from Google Sign-In',
    example: 'eyJhbGciOiJSUzI1NiIsImtpZCI6IjFlOWdkazcifQ...',
    type: String,
  })
  @IsString()
  @IsNotEmpty()
  idToken: string;
}
