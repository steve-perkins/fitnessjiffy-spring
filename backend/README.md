# Fitness Tracker Backend - Phase 2: NestJS Backend Core

NestJS backend application for the Fitness Tracker modernization project.

## Status: Phase 2 Complete ✅

- ✅ NestJS app with TypeORM connected to PostgreSQL
- ✅ 7 TypeORM entities mapped to existing Phase 1 tables
- ✅ TypeORM migrations system configured with baseline
- ✅ Google OAuth + JWT authentication working
- ✅ Basic endpoints: POST /auth/google, GET /users/me
- ✅ Docker Compose for local development

## Tech Stack

- **Framework**: NestJS 10.x
- **Language**: TypeScript 5.x
- **Database ORM**: TypeORM 0.3.x
- **Database**: PostgreSQL 16
- **Authentication**: Google OAuth 2.0 + JWT (Passport.js)
- **Validation**: class-validator, class-transformer
- **Testing**: Jest

## Prerequisites

- Node.js 20.x or later
- PostgreSQL 16 (or use Docker Compose)
- Phase 1 database migration completed

## Installation

```bash
npm install
```

## Configuration

Create `.env` file from `.env.example`:

```bash
cp .env.example .env
```

Update the following variables:
- `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`, `DB_DATABASE` - PostgreSQL connection
- `JWT_SECRET` - Secret key for JWT signing (change in production!)
- `GOOGLE_CLIENT_ID` - Google OAuth 2.0 Client ID

## Running the Application

### Option 1: Local Development (with existing PostgreSQL)

Ensure Phase 1 PostgreSQL is running:

```bash
cd ../migration
docker-compose -f docker-compose.postgres.yml up -d
cd ../backend
```

Start the application:

```bash
npm run start:dev
```

The API will be available at `http://localhost:3000`.

### Option 2: Docker Compose (full stack)

```bash
docker-compose up -d
```

This will start both PostgreSQL and the NestJS backend.

## API Endpoints

### Health Check
- `GET /` - Welcome message
- `GET /health` - Health check endpoint

### Authentication
- `POST /auth/google` - Authenticate with Google ID token
  - Body: `{ "idToken": "..." }`
  - Returns: `{ "access_token": "jwt", "user": {...} }`

### Users
- `GET /users/me` - Get current user profile (requires JWT)
  - Header: `Authorization: Bearer <jwt>`
  - Returns user profile data

## Database Schema

The application connects to the Phase 1 PostgreSQL database with the following tables:

- `users` - User profiles
- `foods` - Food items (global and user-owned)
- `foods_eaten` - Food consumption records
- `exercises` - Exercise types
- `exercises_performed` - Exercise activity records
- `weights` - Weight tracking records
- `report_entries` - Daily summary reports

## Entity Structure

### Critical Business Logic

**FoodEaten.getRatio()** - Complex serving size conversion
- Handles conversion between different serving types (ounces, cups, tablespoons, etc.)
- Ported from Java (FoodEaten.java:157-169)

**ActivityLevel Enum** - Stored as numeric values in database
- Uses custom TypeORM transformer to convert between enum and float
- Values: SEDENTARY (1.25), LIGHTLY_ACTIVE (1.3), MODERATELY_ACTIVE (1.5), VERY_ACTIVE (1.7), EXTREMELY_ACTIVE (2.0)

## TypeORM Migrations

### View Migrations Status

```bash
npm run migration:show
```

### Generate New Migration

After modifying entities:

```bash
npm run migration:generate -- src/migrations/DescriptiveName
```

### Run Migrations

```bash
npm run migration:run
```

### Revert Last Migration

```bash
npm run migration:revert
```

## Testing

```bash
# Unit tests
npm run test

# E2E tests
npm run test:e2e

# Test coverage
npm run test:cov
```

## Project Structure

```
src/
├── main.ts                 # Application entry point
├── app.module.ts           # Root module
├── app.controller.ts       # Root controller
├── app.service.ts          # Root service
│
├── config/                 # Configuration files
│   ├── database.config.ts  # TypeORM configuration
│   └── validation.schema.ts # Environment validation
│
├── common/                 # Shared resources
│   ├── enums/              # Enums (Sex, ActivityLevel, ServingType)
│   ├── decorators/         # Custom decorators (@CurrentUser)
│   └── guards/             # Guards (JwtAuthGuard)
│
├── entities/               # TypeORM entities
│   ├── user.entity.ts
│   ├── food.entity.ts
│   ├── food-eaten.entity.ts
│   ├── exercise.entity.ts
│   ├── exercise-performed.entity.ts
│   ├── weight.entity.ts
│   └── report-entry.entity.ts
│
├── auth/                   # Authentication module
│   ├── auth.module.ts
│   ├── auth.controller.ts
│   ├── auth.service.ts
│   ├── strategies/         # Passport strategies
│   └── dto/                # Data Transfer Objects
│
├── users/                  # Users module
│   ├── users.module.ts
│   ├── users.controller.ts
│   └── users.service.ts
│
└── migrations/             # TypeORM migrations
    └── 1700000000000-InitialSchema.ts
```

## Known Issues / TODO

- TypeORM migration:run requires dotenv package (to be addressed in Phase 3)
- Google OAuth testing requires valid GOOGLE_CLIENT_ID
- Unit and integration tests to be written in Phase 3

## Next Steps (Phase 3)

Phase 3 will implement the core business logic:

1. Users module - Profile operations
2. Weights module - CRUD operations
3. Foods module - Search, visibility logic, CRUD
4. Exercises module - Search, CRUD
5. Reports module - Synchronous report_data updates

## Development Notes

### Building

```bash
npm run build
```

Compiled files will be in the `dist/` directory.

### Linting

```bash
npm run lint
```

### Code Formatting

```bash
npm run format
```

## Contributing

This is the modernization of a legacy Spring Boot application. See the main project plan at `/Users/steve/.claude/plans/buzzing-roaming-barto.md` for the full migration strategy.

## License

ISC
