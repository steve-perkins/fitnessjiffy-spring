import { DataSource } from 'typeorm';
import { newDb } from 'pg-mem';
import { User } from '../../../src/entities/user.entity';
import { Weight } from '../../../src/entities/weight.entity';
import { Food } from '../../../src/entities/food.entity';
import { FoodEaten } from '../../../src/entities/food-eaten.entity';
import { Exercise } from '../../../src/entities/exercise.entity';
import { ExercisePerformed } from '../../../src/entities/exercise-performed.entity';
import { ReportEntry } from '../../../src/entities/report-entry.entity';

/**
 * Creates an in-memory PostgreSQL database using pg-mem
 * with all necessary extensions and entities configured
 */
export async function createTestDataSource(): Promise<DataSource> {
  const db = newDb({
    autoCreateForeignKeyIndices: true,
  });

  // Register uuid-ossp extension for UUID generation
  db.public.registerFunction({
    name: 'uuid_generate_v4',
    returns: db.public.getType('uuid' as any),
    implementation: () => require('crypto').randomUUID(),
    impure: true,
  });

  // Register current_database function (required by TypeORM)
  db.public.registerFunction({
    name: 'current_database',
    returns: db.public.getType('text' as any),
    implementation: () => 'test',
  });

  // Register version function (required by TypeORM)
  db.public.registerFunction({
    name: 'version',
    returns: db.public.getType('text' as any),
    implementation: () =>
      'PostgreSQL 14.0 (pg-mem simulator)',
  });

  // Register pg_trgm extension for fuzzy text search
  db.public.registerFunction({
    name: 'similarity',
    args: [db.public.getType('text' as any), db.public.getType('text' as any)],
    returns: db.public.getType('float' as any),
    implementation: (a: string, b: string) => {
      if (!a || !b) return 0;
      const aLower = a.toLowerCase();
      const bLower = b.toLowerCase();

      // Simple similarity: exact match = 1.0, contains = 0.5, else 0
      if (aLower === bLower) return 1.0;
      if (aLower.includes(bLower) || bLower.includes(aLower)) return 0.5;

      // Check for word starts (e.g., "chick" matches "Chicken Breast")
      const bWords = bLower.split(/\s+/);
      const aWords = aLower.split(/\s+/);
      for (const bWord of bWords) {
        for (const aWord of aWords) {
          if (aWord.startsWith(bWord) || bWord.startsWith(aWord)) {
            return 0.4;
          }
        }
      }

      return 0;
    },
  });

  // Create TypeORM DataSource
  const dataSource = await db.adapters.createTypeormDataSource({
    type: 'postgres',
    entities: [
      User,
      Weight,
      Food,
      FoodEaten,
      Exercise,
      ExercisePerformed,
      ReportEntry,
    ],
    synchronize: true, // Auto-create schema
    logging: false,
  });

  await dataSource.initialize();
  return dataSource;
}

/**
 * Cleans up the test database and closes connections
 */
export async function destroyTestDataSource(dataSource: DataSource): Promise<void> {
  if (dataSource && dataSource.isInitialized) {
    await dataSource.destroy();
  }
}
