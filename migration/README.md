# Database Migration: MySQL → PostgreSQL

This directory contains scripts and instructions for migrating the FitnessJiffy MySQL database to PostgreSQL.

## Overview

**Changes**:
- MySQL `BINARY(16)` UUIDs → PostgreSQL native `UUID` type
- `gender` column → `sex` column
- Remove `net_points` from `report_entries` (calories tracking only)
- `TIMESTAMP` → `TIMESTAMP WITH TIME ZONE`
- **Pluralized table names** (users, foods, exercises, etc.)

**Table Name Mapping**:
```
MySQL (singular/prefixed)     →  PostgreSQL (plural)
─────────────────────────────────────────────────────
fitnessjiffy_user             →  users
food                          →  foods
food_eaten                    →  foods_eaten
exercise                      →  exercises
exercise_performed            →  exercises_performed
weight                        →  weights
report_data                   →  report_entries
```

**Migration Steps**:
1. Set up local PostgreSQL
2. Export MySQL data via SSH tunnel
3. Convert CSV files (handles table renaming)
4. Import to PostgreSQL
5. Validate data integrity
6. Update user email to Gmail

---

## Prerequisites

- Docker and Docker Compose
- Python 3.7+
- DBeaver (for MySQL export)
- Access to production MySQL database

---

## Step 1: Set Up Local PostgreSQL

Start PostgreSQL container:

```bash
cd migration
docker-compose -f docker-compose.postgres.yml up -d
```

Verify it's running:

```bash
docker-compose -f docker-compose.postgres.yml ps
```

The schema will be automatically created on first start.

**Connection details**:
- Host: `localhost`
- Port: `5432`
- Database: `fitness_tracker`
- User: `fitness_user`
- Password: `fitness_dev_password` (default, can override with `POSTGRES_PASSWORD` env var)

**Optional**: Start pgAdmin for GUI access:

```bash
docker-compose -f docker-compose.postgres.yml --profile tools up -d
```

Access pgAdmin at: http://localhost:5050 (admin@localhost / admin)

---

## Step 2: Export MySQL Data Using DBeaver

### Connect to MySQL Database

1. Open DBeaver
2. Create connection to your production MySQL database
   - Configure SSL settings as needed
   - Test connection

### Export Each Table to CSV

For each of the following tables, export to CSV:

- `fitnessjiffy_user`
- `food`
- `food_eaten`
- `exercise`
- `exercise_performed`
- `weight`
- `report_data`

### Export Instructions (Per Table):

1. **Select the table** in Database Navigator
2. **Right-click** → **Export Data**
3. Choose **CSV** format
4. Click **Next**
5. Configure export settings:
   - **Format**: CSV
   - **Header**: Check "Export header"
   - **Delimiter**: Comma (`,`)
   - **Quote character**: Double quote (`"`)
   - **Null string**: `NULL` or leave empty
   - **Encoding**: UTF-8
6. **Output file**: Save to `migration/` directory as `<table_name>.csv`
   - Example: `fitnessjiffy_user.csv`
7. Click **Proceed** → **Start**

### Example for fitnessjiffy_user:

1. Right-click `fitnessjiffy_user` table
2. Export Data → CSV
3. Save as: `/path/to/migration/fitnessjiffy_user.csv`
4. Ensure "Export header" is checked

Repeat for all 7 tables.

### Verify Exports

After exporting, you should have:

```
migration/
├── fitnessjiffy_user.csv
├── food.csv
├── food_eaten.csv
├── exercise.csv
├── exercise_performed.csv
├── weight.csv
└── report_data.csv
```

---

## Step 3: Convert CSV Files

Run the Python conversion script:

```bash
cd migration
python3 convert_mysql_to_postgres.py
```

This script will:
- Convert `BINARY(16)` UUIDs to string format
- Rename `gender` → `sex` in user table
- Remove `net_points` from report_data
- Convert timestamps to ISO format
- Output PostgreSQL-compatible CSV files (`*_pg.csv`)

**Output files**:
```
migration/
├── fitnessjiffy_user_pg.csv
├── food_pg.csv
├── food_eaten_pg.csv
├── exercise_pg.csv
├── exercise_performed_pg.csv
├── weight_pg.csv
└── report_data_pg.csv
```

---

## Step 4: Import to PostgreSQL

**Option 1: Using Python script** (Recommended - no psql required):

```bash
cd migration
python3 import_postgres.py
```

The script will prompt for the PostgreSQL password (default: `fitness_dev_password`).

Alternatively, set password as environment variable:

```bash
export POSTGRES_PASSWORD='fitness_dev_password'
python3 import_postgres.py
```

**Option 2: Using bash script** (Requires psql CLI tools):

```bash
export PGPASSWORD='fitness_dev_password'
./import_postgres.sh
```

Both scripts import tables in dependency order (using new plural names):
1. users
2. exercises
3. foods
4. weights
5. foods_eaten
6. exercises_performed
7. report_entries

---

## Step 5: Validate Data Integrity

### Connect to PostgreSQL

```bash
docker-compose -f docker-compose.postgres.yml exec postgres psql -U fitness_user -d fitness_tracker
```

### Run Validation Queries

#### 1. Check Row Counts

```sql
SELECT 'users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'foods', COUNT(*) FROM foods
UNION ALL
SELECT 'foods_eaten', COUNT(*) FROM foods_eaten
UNION ALL
SELECT 'exercises', COUNT(*) FROM exercises
UNION ALL
SELECT 'exercises_performed', COUNT(*) FROM exercises_performed
UNION ALL
SELECT 'weights', COUNT(*) FROM weights
UNION ALL
SELECT 'report_entries', COUNT(*) FROM report_entries;
```

Compare these counts to MySQL:

```sql
-- Run in MySQL
SELECT 'fitnessjiffy_user' as table_name, COUNT(*) as count FROM fitnessjiffy_user
UNION ALL
SELECT 'food', COUNT(*) FROM food
UNION ALL
SELECT 'food_eaten', COUNT(*) FROM food_eaten
UNION ALL
SELECT 'exercise', COUNT(*) FROM exercise
UNION ALL
SELECT 'exercise_performed', COUNT(*) FROM exercise_performed
UNION ALL
SELECT 'weight', COUNT(*) FROM weight
UNION ALL
SELECT 'report_data', COUNT(*) FROM report_data;
```

#### 2. Verify Date Range (20 years)

```sql
SELECT
  MIN(date) as earliest_date,
  MAX(date) as latest_date,
  (MAX(date) - MIN(date)) as days_span
FROM weights;
```

Should show ~20 years of data.

#### 3. Check Foreign Key Integrity

```sql
-- Check for orphaned foods
SELECT COUNT(*) FROM foods
WHERE owner_id IS NOT NULL
  AND owner_id NOT IN (SELECT id FROM users);
-- Should return 0

-- Check for orphaned foods_eaten records
SELECT COUNT(*) FROM foods_eaten fe
LEFT JOIN users u ON fe.user_id = u.id
LEFT JOIN foods f ON fe.food_id = f.id
WHERE u.id IS NULL OR f.id IS NULL;
-- Should return 0

-- Check for orphaned exercises_performed records
SELECT COUNT(*) FROM exercises_performed ep
LEFT JOIN users u ON ep.user_id = u.id
LEFT JOIN exercises e ON ep.exercise_id = e.id
WHERE u.id IS NULL OR e.id IS NULL;
-- Should return 0
```

#### 4. Verify sex Column (renamed from gender)

```sql
SELECT sex, COUNT(*) FROM users GROUP BY sex;
```

Should show MALE or FEMALE (enum values).

#### 5. Verify report_entries (no net_points)

```sql
-- Check schema (list all columns)
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'report_entries'
ORDER BY ordinal_position;
```

Should show only: `id`, `user_id`, `date`, `pounds`, `net_calories` (no `net_points`).

#### 6. Spot-Check Specific Dates

Pick 5-10 random dates and compare foods_eaten records between MySQL and PostgreSQL:

```sql
-- PostgreSQL
SELECT fe.date, f.name, fe.serving_qty, fe.serving_type
FROM foods_eaten fe
JOIN foods f ON fe.food_id = f.id
WHERE fe.date = '2024-01-15'  -- Pick known dates
ORDER BY f.name;
```

```sql
-- MySQL (compare)
SELECT fe.date, f.name, fe.serving_qty, fe.serving_type
FROM food_eaten fe
JOIN food f ON fe.food_id = f.id
WHERE fe.date = '2024-01-15'
ORDER BY f.name;
```

Should match exactly.

---

## Step 6: Update User Email

Update the user's email to their Gmail address and clear password:

```sql
-- Connect to PostgreSQL
docker-compose -f docker-compose.postgres.yml exec postgres psql -U fitness_user -d fitness_tracker
```

```sql
-- Find current user
SELECT id, email, first_name, last_name FROM users;

-- Update email and clear password
UPDATE users
SET email = 'your-gmail-address@gmail.com',
    password_hash = NULL
WHERE id = '<your-user-id>';  -- Replace with actual UUID

-- Verify
SELECT id, email, password_hash FROM users;
```

---

## Step 7: Create Backup

Once validated, create a PostgreSQL backup:

```bash
docker-compose -f docker-compose.postgres.yml exec postgres \
  pg_dump -U fitness_user -d fitness_tracker -F c \
  > ../fitness_tracker_backup_$(date +%Y%m%d).dump
```

This creates a compressed backup file.

---

## Troubleshooting

### Connection Issues

If import fails with connection error:

```bash
# Check PostgreSQL is running
docker-compose -f docker-compose.postgres.yml ps

# Check logs
docker-compose -f docker-compose.postgres.yml logs postgres

# Restart if needed
docker-compose -f docker-compose.postgres.yml restart postgres
```

### UUID Conversion Errors

If UUIDs don't convert properly, check the export format:

1. Open one of the original CSV files (`fitnessjiffy_user.csv`)
2. Check the `id` column format
3. If it's not hex (e.g., shows as binary garbage), try re-exporting with different settings in DBeaver

### Import Fails Due to Foreign Keys

Tables must be imported in dependency order (handled by `import_postgres.py`):

1. users (no dependencies)
2. exercises (no dependencies)
3. foods (references users)
4. weights (references users)
5. foods_eaten (references users + foods)
6. exercises_performed (references users + exercises)
7. report_entries (references users)

---

## Cleanup

### Remove CSV Files (After Successful Migration)

```bash
# Keep backups, remove working files
rm *_pg.csv  # Keep originals for safety
```

### Stop PostgreSQL

```bash
docker-compose -f docker-compose.postgres.yml down
```

To remove data volumes (WARNING: deletes all data):

```bash
docker-compose -f docker-compose.postgres.yml down -v
```

---

## Next Steps

After successful migration:

1. ✅ PostgreSQL database ready with 20 years of data
2. ✅ User email updated to Gmail
3. ✅ Data validated

Continue to **Phase 2**: NestJS Backend Core

See main modernization plan at: `~/.claude/plans/buzzing-roaming-barto.md`
