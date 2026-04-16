#!/usr/bin/env python3
"""
Convert MySQL CSV exports to PostgreSQL-compatible format

Handles:
1. BINARY(16) UUID → string UUID format
2. TIMESTAMP format conversion
3. gender column → sex column
4. Remove net_points column from report_data
5. NULL value handling
"""

import csv
import sys
import uuid
from datetime import datetime
from pathlib import Path


def binary_to_uuid(binary_str):
    """Convert MySQL BINARY(16) hex string to UUID string format"""
    if not binary_str or binary_str in ('NULL', '\\N', ''):
        return None

    # Remove 0x prefix if present
    hex_str = binary_str.replace('0x', '').replace('0X', '')

    # Handle different export formats
    if len(hex_str) == 32:  # Raw hex string
        return str(uuid.UUID(hex=hex_str))
    elif len(hex_str) == 16:  # Already in binary form
        return str(uuid.UUID(bytes=bytes.fromhex(hex_str)))
    else:
        print(f"Warning: Unexpected UUID format: {binary_str}", file=sys.stderr)
        return None


def convert_timestamp(timestamp_str):
    """Convert MySQL TIMESTAMP to PostgreSQL TIMESTAMP WITH TIME ZONE format"""
    if not timestamp_str or timestamp_str in ('NULL', '\\N', ''):
        return None

    try:
        # MySQL format: YYYY-MM-DD HH:MM:SS
        dt = datetime.strptime(timestamp_str, '%Y-%m-%d %H:%M:%S')
        # PostgreSQL format (ISO 8601)
        return dt.isoformat()
    except ValueError:
        # Try alternative format
        try:
            dt = datetime.fromisoformat(timestamp_str)
            return dt.isoformat()
        except:
            print(f"Warning: Could not parse timestamp: {timestamp_str}", file=sys.stderr)
            return timestamp_str


def convert_csv(input_file, output_file, table_name):
    """Convert MySQL CSV to PostgreSQL-compatible CSV"""

    # UUID columns per table
    uuid_columns_map = {
        'fitnessjiffy_user': {'id'},
        'food': {'id', 'owner_id'},
        'food_eaten': {'id', 'user_id', 'food_id'},
        'exercise': {'id'},
        'exercise_performed': {'id', 'user_id', 'exercise_id'},
        'weight': {'id', 'user_id'},
        'report_data': {'id', 'user_id'},
    }

    # Timestamp columns
    timestamp_columns = {'created_time', 'last_updated_time'}

    uuid_columns = uuid_columns_map.get(table_name, set())

    print(f"Converting {table_name}...")

    input_path = Path(input_file)
    if not input_path.exists():
        print(f"Warning: Input file not found: {input_file}", file=sys.stderr)
        return

    with open(input_file, 'r', encoding='utf-8') as infile, \
         open(output_file, 'w', newline='', encoding='utf-8') as outfile:

        reader = csv.DictReader(infile)
        if not reader.fieldnames:
            print(f"Warning: No headers found in {input_file}", file=sys.stderr)
            return

        fieldnames = list(reader.fieldnames)

        # Special handling for fitnessjiffy_user: rename gender → sex
        if table_name == 'fitnessjiffy_user':
            if 'gender' in fieldnames:
                fieldnames[fieldnames.index('gender')] = 'sex'

        # Special handling for report_data: remove net_points
        if table_name == 'report_data':
            if 'net_points' in fieldnames:
                fieldnames.remove('net_points')

        writer = csv.DictWriter(outfile, fieldnames=fieldnames)
        writer.writeheader()

        row_count = 0
        for row in reader:
            converted_row = {}

            for col in fieldnames:
                # Handle renamed column
                source_col = 'gender' if col == 'sex' and table_name == 'fitnessjiffy_user' else col

                # Skip net_points if it's report_data
                if col == 'net_points' and table_name == 'report_data':
                    continue

                value = row.get(source_col, '')

                # Convert UUIDs
                if col in uuid_columns:
                    value = binary_to_uuid(value)

                # Convert timestamps
                elif col in timestamp_columns:
                    value = convert_timestamp(value)

                # Handle NULL values
                if value in ('NULL', '\\N', None):
                    value = ''  # PostgreSQL COPY treats empty as NULL

                converted_row[col] = value if value is not None else ''

            writer.writerow(converted_row)
            row_count += 1

        print(f"  ✓ Converted {row_count} rows")


def main():
    """Main conversion process"""

    tables = [
        'fitnessjiffy_user',
        'food',
        'food_eaten',
        'exercise',
        'exercise_performed',
        'weight',
        'report_data',
    ]

    print("MySQL to PostgreSQL CSV Conversion")
    print("=" * 50)
    print()

    # Check for input files
    missing_files = []
    for table in tables:
        input_file = f'{table}.csv'
        if not Path(input_file).exists():
            missing_files.append(input_file)

    if missing_files:
        print("Warning: The following input files are missing:")
        for f in missing_files:
            print(f"  - {f}")
        print()
        print("Please export these tables from MySQL using DBeaver.")
        print("See README.md for detailed instructions.")
        print()

    # Convert existing files
    converted_count = 0
    for table in tables:
        input_file = f'{table}.csv'
        output_file = f'{table}_pg.csv'

        if Path(input_file).exists():
            convert_csv(input_file, output_file, table)
            converted_count += 1
        else:
            print(f"Skipping {table} (no input file)")

    print()
    print("=" * 50)
    print(f"Conversion complete! Converted {converted_count}/{len(tables)} tables.")
    print()
    print("Next steps:")
    print("1. Review the *_pg.csv files")
    print("2. Run import_postgres.sh to load data into PostgreSQL")


if __name__ == '__main__':
    main()
