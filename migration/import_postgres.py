#!/usr/bin/env python3
"""
Import converted CSV files into PostgreSQL

Imports PostgreSQL-compatible CSV files (output from convert_mysql_to_postgres.py)
into local PostgreSQL database running in Docker.

Usage:
    python3 import_postgres.py
"""

import sys
from pathlib import Path

try:
    import psycopg2
except ImportError:
    print("Required package not installed.")
    print("Run: pip3 install psycopg2-binary")
    sys.exit(1)


# PostgreSQL Configuration (local development only)
POSTGRES_HOST = 'localhost'
POSTGRES_PORT = 5432
POSTGRES_DATABASE = 'fitness_tracker'
POSTGRES_USER = 'fitness_user'
POSTGRES_PASSWORD = 'fitness_dev_password'  # Default from docker-compose.postgres.yml

# Tables in dependency order (respects foreign keys)
# Using new PostgreSQL plural table names
TABLES = [
    'users',               # No dependencies
    'exercises',           # No dependencies
    'foods',               # References users (nullable owner_id)
    'weights',             # References users
    'foods_eaten',         # References users + foods
    'exercises_performed', # References users + exercises
    'report_entries',      # References users
]


def import_table(connection, table_name):
    """Import a single table from CSV file"""

    csv_file = f"{table_name}_pg.csv"
    csv_path = Path(csv_file)

    if not csv_path.exists():
        print(f"⚠ Warning: {csv_file} not found, skipping...")
        return 0

    print(f"Importing {table_name}...", end=' ', flush=True)

    with connection.cursor() as cursor:
        # Read CSV header to get column names in the correct order
        with open(csv_file, 'r', encoding='utf-8') as f:
            # Read first line to get column names
            first_line = f.readline().strip()
            column_names = first_line

            # Reset to beginning for COPY
            f.seek(0)

            # Use COPY FROM with explicit column list from CSV header
            # This ensures columns are mapped correctly regardless of table column order
            cursor.copy_expert(
                f"COPY {table_name} ({column_names}) FROM STDIN WITH CSV HEADER NULL ''",
                f
            )

        # Count rows
        cursor.execute(f"SELECT COUNT(*) FROM {table_name}")
        count = cursor.fetchone()[0]

        print(f"✓ {count} rows")

    connection.commit()
    return count


def main():
    print(f"\n{'='*60}")
    print("PostgreSQL Data Import")
    print(f"{'='*60}\n")
    print(f"Database: {POSTGRES_DATABASE}")
    print(f"Host: {POSTGRES_HOST}:{POSTGRES_PORT}")
    print(f"User: {POSTGRES_USER}")
    print()

    try:
        # Connect to PostgreSQL
        print("Connecting to PostgreSQL...", flush=True)
        connection = psycopg2.connect(
            host=POSTGRES_HOST,
            port=POSTGRES_PORT,
            database=POSTGRES_DATABASE,
            user=POSTGRES_USER,
            password=POSTGRES_PASSWORD,
        )
        print("✓ Connected to PostgreSQL\n")

        # Check for missing CSV files
        missing_files = []
        for table in TABLES:
            csv_file = f'{table}_pg.csv'
            if not Path(csv_file).exists():
                missing_files.append(csv_file)

        if missing_files:
            print("⚠ Warning: The following CSV files are missing:")
            for f in missing_files:
                print(f"  - {f}")
            print()

        # Import tables
        print("Starting import...")
        print()

        total_rows = 0
        imported_count = 0
        for table_name in TABLES:
            row_count = import_table(connection, table_name)
            if row_count > 0:
                total_rows += row_count
                imported_count += 1

        # Close connection
        connection.close()

        print()
        print(f"{'='*60}")
        print(f"Import complete! {total_rows} total rows imported across {imported_count} tables.")
        print(f"{'='*60}\n")
        print("Next steps:")
        print("1. Run validation queries (see README.md)")
        print("2. Update user email to Gmail")
        print("3. Verify data integrity")

    except psycopg2.OperationalError as e:
        print(f"\n✗ Error: Cannot connect to PostgreSQL", file=sys.stderr)
        print(f"   {e}", file=sys.stderr)
        print()
        print("Please ensure:")
        print("  1. PostgreSQL is running (docker-compose -f docker-compose.postgres.yml up -d)")
        print("  2. Database 'fitness_tracker' exists")
        print("  3. Correct password provided")
        sys.exit(1)
    except Exception as e:
        print(f"\n✗ Error: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == '__main__':
    main()
