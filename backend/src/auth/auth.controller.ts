import { Controller, Post, Body, HttpCode } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse } from '@nestjs/swagger';
import { AuthService } from './auth.service';
import { GoogleLoginDto } from './dto/google-login.dto';
import { DevTokenDto } from './dto/dev-token.dto';

@ApiTags('auth')
@Controller('auth')
export class AuthController {
  constructor(private authService: AuthService) {}

  @ApiOperation({ summary: 'Authenticate with Google OAuth token' })
  @ApiResponse({
    status: 200,
    description: 'Returns JWT token and user profile',
  })
  @ApiResponse({
    status: 401,
    description: 'Invalid Google token or user not found',
  })
  @Post('google')
  @HttpCode(200)
  async googleLogin(@Body() dto: GoogleLoginDto) {
    return this.authService.verifyGoogleToken(dto.idToken);
  }

  /**
   * DEVELOPMENT ONLY: Generate JWT for testing
   * Disabled in production via environment check in service
   * Usage: POST /auth/dev-token { "email": "user@example.com" }
   */
  @ApiOperation({
    summary: 'Generate dev JWT token (development only)',
    description:
      'Generates a JWT token for testing without Google OAuth. Only available when NODE_ENV !== production.',
  })
  @ApiResponse({
    status: 200,
    description: 'Returns JWT token for testing',
  })
  @ApiResponse({
    status: 401,
    description: 'User not found or disabled in production',
  })
  @Post('dev-token')
  @HttpCode(200)
  async generateDevToken(@Body() dto: DevTokenDto) {
    return this.authService.generateTestToken(dto.email);
  }
}
