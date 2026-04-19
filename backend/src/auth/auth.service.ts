import { Injectable, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { OAuth2Client } from 'google-auth-library';
import { ConfigService } from '@nestjs/config';
import { User } from '../entities/user.entity';

@Injectable()
export class AuthService {
  private googleClient: OAuth2Client;

  constructor(
    @InjectRepository(User) private userRepository: Repository<User>,
    private jwtService: JwtService,
    private configService: ConfigService,
  ) {
    this.googleClient = new OAuth2Client(
      configService.get('GOOGLE_CLIENT_ID'),
    );
  }

  async verifyGoogleToken(idToken: string) {
    try {
      // Verify token with Google
      const ticket = await this.googleClient.verifyIdToken({
        idToken,
        audience: this.configService.get('GOOGLE_CLIENT_ID'),
      });

      const payload = ticket.getPayload();
      const email = payload?.email;

      if (!email) {
        throw new UnauthorizedException('Email not found in token');
      }

      // Find user by email
      const user = await this.userRepository.findOne({ where: { email } });

      if (!user) {
        throw new UnauthorizedException('User not found');
      }

      // Generate JWT with user information
      const jwtPayload = {
        sub: user.id,
        email: user.email,
        firstName: user.firstName,
        lastName: user.lastName,
      };
      const accessToken = this.jwtService.sign(jwtPayload);

      return {
        access_token: accessToken,
        user: {
          id: user.id,
          email: user.email,
          firstName: user.firstName,
          lastName: user.lastName,
        },
      };
    } catch (error) {
      throw new UnauthorizedException('Invalid Google token');
    }
  }

  /**
   * DEVELOPMENT ONLY: Generate JWT for testing without Google OAuth
   * This endpoint should be disabled in production via environment variable
   */
  async generateTestToken(email: string) {
    // Only allow in development environment
    if (this.configService.get('NODE_ENV') === 'production') {
      throw new UnauthorizedException(
        'Test token generation is disabled in production',
      );
    }

    // Find user by email
    const user = await this.userRepository.findOne({ where: { email } });

    if (!user) {
      throw new UnauthorizedException('User not found');
    }

    // Generate JWT with user information
    const jwtPayload = {
      sub: user.id,
      email: user.email,
      firstName: user.firstName,
      lastName: user.lastName,
    };
    const accessToken = this.jwtService.sign(jwtPayload);

    return {
      access_token: accessToken,
      user: {
        id: user.id,
        email: user.email,
        firstName: user.firstName,
        lastName: user.lastName,
      },
    };
  }
}
