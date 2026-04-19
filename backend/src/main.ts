import { NestFactory } from '@nestjs/core';
import { ValidationPipe } from '@nestjs/common';
import { SwaggerModule, DocumentBuilder } from '@nestjs/swagger';
import { AppModule } from './app.module';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  // Enable CORS for frontend development
  app.enableCors({
    origin: [
      'http://localhost:5173',
      'http://localhost:3001',
      'http://localhost:3000',
      'http://localhost:8080', // Python http.server for testing
      'null', // Allow local file:// testing
    ],
    credentials: true,
  });

  // Enable global validation pipes
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      transform: true,
      forbidNonWhitelisted: true,
    }),
  );

  // Configure Swagger/OpenAPI documentation
  const config = new DocumentBuilder()
    .setTitle('Fitness Tracker API')
    .setDescription(
      'REST API for fitness tracking with foods, exercises, weights, and reports',
    )
    .setVersion('1.0')
    .addBearerAuth(
      { type: 'http', scheme: 'bearer', bearerFormat: 'JWT' },
      'JWT-auth',
    )
    .addServer('http://localhost:3000', 'Local development')
    .addTag('auth', 'Authentication endpoints (Google OAuth + Dev Token)')
    .addTag('users', 'User profile management')
    .addTag('weights', 'Weight tracking')
    .addTag('foods', 'Food management and food diary')
    .addTag('exercises', 'Exercise management and exercise diary')
    .addTag('reports', 'Daily report entries with calories and weight')
    .build();

  const document = SwaggerModule.createDocument(app, config);
  SwaggerModule.setup('api/docs', app, document, {
    swaggerOptions: {
      persistAuthorization: true, // Remember JWT token in browser
    },
  });

  const port = process.env.PORT || 3000;
  await app.listen(port);
  console.log(`Application is running on: http://localhost:${port}`);
}

bootstrap();
