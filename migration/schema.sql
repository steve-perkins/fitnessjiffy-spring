-- PostgreSQL Schema for Fitness Tracker
-- Migrated from MySQL FitnessJiffy schema
-- Changes:
--   1. BINARY(16) UUIDs → native UUID type
--   2. gender column → sex column
--   3. Removed net_points from report_entries (calories only)
--   4. TIMESTAMP → TIMESTAMP WITH TIME ZONE
--   5. Pluralized table names (users, foods, exercises, weights, etc.)

-- Drop tables if they exist (for clean re-import)
DROP TABLE IF EXISTS report_entries CASCADE;
DROP TABLE IF EXISTS exercises_performed CASCADE;
DROP TABLE IF EXISTS foods_eaten CASCADE;
DROP TABLE IF EXISTS weights CASCADE;
DROP TABLE IF EXISTS exercises CASCADE;
DROP TABLE IF EXISTS foods CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Drop types if they exist
DROP TYPE IF EXISTS sex_enum CASCADE;
DROP TYPE IF EXISTS serving_type_enum CASCADE;

-- Enable extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";  -- UUID generation functions
CREATE EXTENSION IF NOT EXISTS pg_trgm;  -- Trigram similarity for fuzzy search

-- Create enums
CREATE TYPE sex_enum AS ENUM ('MALE', 'FEMALE');

CREATE TYPE serving_type_enum AS ENUM (
    'OUNCE',
    'CUP',
    'POUND',
    'PINT',
    'TABLESPOON',
    'TEASPOON',
    'GRAM',
    'CUSTOM'
);

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sex sex_enum NOT NULL,
    birthdate DATE NOT NULL,
    height_in_inches DOUBLE PRECISION NOT NULL,
    activity_level DOUBLE PRECISION NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(100),  -- Nullable, will be NULL after migration
    first_name VARCHAR(20) NOT NULL,
    last_name VARCHAR(20) NOT NULL,
    timezone VARCHAR(50) NOT NULL,
    created_time TIMESTAMP WITH TIME ZONE NOT NULL,
    last_updated_time TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_user_email ON users(email);

-- Foods table
CREATE TABLE foods (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_id UUID REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    default_serving_type serving_type_enum NOT NULL,
    serving_type_qty DOUBLE PRECISION NOT NULL,
    calories INTEGER NOT NULL,
    fat DOUBLE PRECISION NOT NULL,
    saturated_fat DOUBLE PRECISION NOT NULL,
    carbs DOUBLE PRECISION NOT NULL,
    fiber DOUBLE PRECISION NOT NULL,
    sugar DOUBLE PRECISION NOT NULL,
    protein DOUBLE PRECISION NOT NULL,
    sodium DOUBLE PRECISION NOT NULL,
    created_time TIMESTAMP WITH TIME ZONE NOT NULL,
    last_updated_time TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE(id, owner_id)
);

CREATE INDEX idx_food_owner ON foods(owner_id);
CREATE INDEX idx_food_name ON foods(name);
CREATE INDEX idx_food_name_lower ON foods(LOWER(name));  -- For case-insensitive searches
CREATE INDEX idx_foods_name_trgm ON foods USING GIN (name gin_trgm_ops);  -- For fuzzy search

-- Foods eaten (junction table)
CREATE TABLE foods_eaten (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    food_id UUID NOT NULL REFERENCES foods(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    serving_type serving_type_enum NOT NULL,
    serving_qty DOUBLE PRECISION NOT NULL,
    UNIQUE(user_id, food_id, date)
);

CREATE INDEX idx_food_eaten_user_date ON foods_eaten(user_id, date);
CREATE INDEX idx_food_eaten_date ON foods_eaten(date);

-- Exercises table
CREATE TABLE exercises (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(5) NOT NULL,
    metabolic_equivalent DOUBLE PRECISION NOT NULL,
    category VARCHAR(25) NOT NULL,
    description VARCHAR(250) NOT NULL
);

CREATE INDEX idx_exercise_category ON exercises(category);
CREATE INDEX idx_exercise_description ON exercises(description);
CREATE INDEX idx_exercise_description_lower ON exercises(LOWER(description));  -- For case-insensitive searches
CREATE INDEX idx_exercises_desc_trgm ON exercises USING GIN (description gin_trgm_ops);  -- For fuzzy search

-- Exercises performed (junction table)
CREATE TABLE exercises_performed (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    exercise_id UUID NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    minutes INTEGER NOT NULL,
    UNIQUE(user_id, exercise_id, date)
);

CREATE INDEX idx_exercise_performed_user_date ON exercises_performed(user_id, date);
CREATE INDEX idx_exercise_performed_date ON exercises_performed(date);

-- Weights table
CREATE TABLE weights (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    pounds DOUBLE PRECISION NOT NULL,
    UNIQUE(user_id, date)
);

CREATE INDEX idx_weight_user_date ON weights(user_id, date);
CREATE INDEX idx_weight_date ON weights(date);

-- Report entries table (denormalized daily summaries)
-- NOTE: net_points column removed - tracking calories only
CREATE TABLE report_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    pounds DOUBLE PRECISION NOT NULL DEFAULT 0,
    net_calories INTEGER NOT NULL DEFAULT 0,
    UNIQUE(user_id, date)
);

CREATE INDEX idx_report_entries_user_date ON report_entries(user_id, date);
CREATE INDEX idx_report_entries_date ON report_entries(date);

-- Grant permissions (adjust user as needed)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO fitness_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO fitness_user;
