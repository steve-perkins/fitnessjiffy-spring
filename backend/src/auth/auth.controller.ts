import { Controller, Post, Body, HttpCode } from '@nestjs/common';
import { AuthService } from './auth.service';
import { GoogleLoginDto } from './dto/google-login.dto';

@Controller('auth')
export class AuthController {
  constructor(private authService: AuthService) {}

  @Post('google')
  @HttpCode(200)
  async googleLogin(@Body() dto: GoogleLoginDto) {
    return this.authService.verifyGoogleToken(dto.idToken);
  }
}
