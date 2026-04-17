import { MigrationInterface, QueryRunner } from 'typeorm';

/**
 * Initial baseline migration for Phase 2
 *
 * This is a no-op migration because the database schema was already created
 * in Phase 1 (migration/schema.sql). This migration establishes the baseline
 * for TypeORM's migration tracking system, allowing us to track future schema
 * changes.
 *
 * The existing schema includes:
 * - users, foods, foods_eaten, exercises, exercises_performed, weights, report_entries tables
 * - All with proper plural naming convention
 * - pg_trgm extension for fuzzy text search
 * - Appropriate indexes and constraints
 */
export class InitialSchema1700000000000 implements MigrationInterface {
  public async up(queryRunner: QueryRunner): Promise<void> {
    // No-op: Schema already exists from Phase 1
    // This migration just establishes the baseline for tracking future changes
  }

  public async down(queryRunner: QueryRunner): Promise<void> {
    // No-op: We don't want to drop the existing schema
  }
}
