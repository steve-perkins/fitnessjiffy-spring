-- PostgreSQL Schema for Fitness Tracker
-- Migrated from MySQL FitnessJiffy schema
-- Changes:
--   1. BINARY(16) UUIDs → native UUID type
--   2. gender column → sex column
--   3. Removed net_points from report_data (calories only)
--   4. TIMESTAMP → TIMESTAMP WITH TIME ZONE

-- Drop tables if they exist (for clean re-import)
DROP TABLE IF EXISTS report_data CASCADE;
DROP TABLE IF EXISTS exercise_performed CASCADE;
DROP TABLE IF EXISTS food_eaten CASCADE;
DROP TABLE IF EXISTS weight CASCADE;
DROP TABLE IF EXISTS exercise CASCADE;
DROP TABLE IF EXISTS food CASCADE;
DROP TABLE IF EXISTS fitnessjiffy_user CASCADE;

-- Drop types if they exist
DROP TYPE IF EXISTS sex_enum CASCADE;
DROP TYPE IF EXISTS serving_type_enum CASCADE;

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

-- User table
CREATE TABLE fitnessjiffy_user (
    id UUID PRIMARY KEY,
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

CREATE INDEX idx_user_email ON fitnessjiffy_user(email);

-- Food table
CREATE TABLE food (
    id UUID PRIMARY KEY,
    owner_id UUID REFERENCES fitnessjiffy_user(id) ON DELETE CASCADE,
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

CREATE INDEX idx_food_owner ON food(owner_id);
CREATE INDEX idx_food_name ON food(name);
CREATE INDEX idx_food_name_lower ON food(LOWER(name));  -- For case-insensitive searches

-- Food eaten (junction table)
CREATE TABLE food_eaten (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES fitnessjiffy_user(id) ON DELETE CASCADE,
    food_id UUID NOT NULL REFERENCES food(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    serving_type serving_type_enum NOT NULL,
    serving_qty DOUBLE PRECISION NOT NULL,
    UNIQUE(user_id, food_id, date)
);

CREATE INDEX idx_food_eaten_user_date ON food_eaten(user_id, date);
CREATE INDEX idx_food_eaten_date ON food_eaten(date);

-- Exercise table
CREATE TABLE exercise (
    id UUID PRIMARY KEY,
    code VARCHAR(5) NOT NULL,
    metabolic_equivalent DOUBLE PRECISION NOT NULL,
    category VARCHAR(25) NOT NULL,
    description VARCHAR(250) NOT NULL
);

CREATE INDEX idx_exercise_category ON exercise(category);
CREATE INDEX idx_exercise_description ON exercise(description);
CREATE INDEX idx_exercise_description_lower ON exercise(LOWER(description));  -- For case-insensitive searches

-- Exercise performed (junction table)
CREATE TABLE exercise_performed (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES fitnessjiffy_user(id) ON DELETE CASCADE,
    exercise_id UUID NOT NULL REFERENCES exercise(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    minutes INTEGER NOT NULL,
    UNIQUE(user_id, exercise_id, date)
);

CREATE INDEX idx_exercise_performed_user_date ON exercise_performed(user_id, date);
CREATE INDEX idx_exercise_performed_date ON exercise_performed(date);

-- Weight table
CREATE TABLE weight (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES fitnessjiffy_user(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    pounds DOUBLE PRECISION NOT NULL,
    UNIQUE(user_id, date)
);

CREATE INDEX idx_weight_user_date ON weight(user_id, date);
CREATE INDEX idx_weight_date ON weight(date);

-- Report data table (denormalized daily summaries)
-- NOTE: net_points column removed - tracking calories only
CREATE TABLE report_data (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES fitnessjiffy_user(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    pounds DOUBLE PRECISION NOT NULL DEFAULT 0,
    net_calories INTEGER NOT NULL DEFAULT 0,
    UNIQUE(user_id, date)
);

CREATE INDEX idx_report_data_user_date ON report_data(user_id, date);
CREATE INDEX idx_report_data_date ON report_data(date);

-- Grant permissions (adjust user as needed)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO fitness_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO fitness_user;
