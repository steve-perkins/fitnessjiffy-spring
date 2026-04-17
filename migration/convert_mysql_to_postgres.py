#!/usr/bin/env python3
"""
Convert MySQL CSV exports to PostgreSQL-compatible format

Handles:
1. BINARY(16) UUID → string UUID format
2. TIMESTAMP format conversion
3. gender column → sex column
4. Remove net_points column from report_entries
5. Table name mapping (MySQL singular → PostgreSQL plural)
6. NULL value handling
"""

import csv
import sys
import uuid
from datetime import datetime
from pathlib import Path

# Table name mapping: MySQL → PostgreSQL
TABLE_NAME_MAPPING = {
    'fitnessjiffy_user': 'users',
    'food': 'foods',
    'food_eaten': 'foods_eaten',
    'exercise': 'exercises',
    'exercise_performed': 'exercises_performed',
    'weight': 'weights',
    'report_data': 'report_entries',
}


def binary_to_uuid(hex_str):
    """Convert MySQL HEX UUID to standard UUID format

    The export_mysql_via_ssh.py script exports UUIDs using HEX() function,
    which gives us 32-character hex strings. We convert these to standard
    UUID format with hyphens.
    """
    if not hex_str or hex_str in ('NULL', '\\N', ''):
        return None

    try:
        # Remove any whitespace
        hex_str = hex_str.strip()

        # MySQL HEX() function returns 32-character hex string
        if len(hex_str) == 32:
            # Convert hex string to UUID
            return str(uuid.UUID(hex=hex_str))
        else:
            print(f"Warning: UUID wrong length: {len(hex_str)} chars (expected 32)", file=sys.stderr)
            return None
    except (ValueError, AttributeError) as e:
        print(f"Warning: Could not convert UUID '{hex_str}': {e}", file=sys.stderr)
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


def convert_csv(input_file, output_file, mysql_table_name, postgres_table_name):
    """Convert MySQL CSV to PostgreSQL-compatible CSV"""

    # UUID columns per table (using MySQL table names for input)
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

    uuid_columns = uuid_columns_map.get(mysql_table_name, set())

    print(f"Converting {mysql_table_name} → {postgres_table_name}...")

    input_path = Path(input_file)
    if not input_path.exists():
        print(f"Warning: Input file not found: {input_file}", file=sys.stderr)
        return

    # Use UTF-8 encoding for clean HEX UUID strings from export script
    with open(input_file, 'r', encoding='utf-8') as infile, \
         open(output_file, 'w', newline='', encoding='utf-8') as outfile:

        reader = csv.DictReader(infile)
        if not reader.fieldnames:
            print(f"Warning: No headers found in {input_file}", file=sys.stderr)
            return

        fieldnames = list(reader.fieldnames)

        # Special handling for fitnessjiffy_user: rename gender → sex
        if mysql_table_name == 'fitnessjiffy_user':
            if 'gender' in fieldnames:
                fieldnames[fieldnames.index('gender')] = 'sex'

        # Special handling for report_data: remove net_points
        if mysql_table_name == 'report_data':
            if 'net_points' in fieldnames:
                fieldnames.remove('net_points')

        writer = csv.DictWriter(outfile, fieldnames=fieldnames)
        writer.writeheader()

        row_count = 0
        for row in reader:
            converted_row = {}

            for col in fieldnames:
                # Handle renamed column
                source_col = 'gender' if col == 'sex' and mysql_table_name == 'fitnessjiffy_user' else col

                # Skip net_points if it's report_data
                if col == 'net_points' and mysql_table_name == 'report_data':
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

    # MySQL table names (input CSVs)
    mysql_tables = list(TABLE_NAME_MAPPING.keys())

    print("MySQL to PostgreSQL CSV Conversion")
    print("=" * 50)
    print()

    # Check for input files
    missing_files = []
    for mysql_table in mysql_tables:
        input_file = f'{mysql_table}.csv'
        if not Path(input_file).exists():
            missing_files.append(input_file)

    if missing_files:
        print("Warning: The following input files are missing:")
        for f in missing_files:
            print(f"  - {f}")
        print()
        print("Please export these tables from MySQL using export_mysql_via_ssh.py.")
        print("See README.md for detailed instructions.")
        print()

    # Convert existing files
    converted_count = 0
    for mysql_table, postgres_table in TABLE_NAME_MAPPING.items():
        input_file = f'{mysql_table}.csv'
        output_file = f'{postgres_table}_pg.csv'

        if Path(input_file).exists():
            convert_csv(input_file, output_file, mysql_table, postgres_table)
            converted_count += 1
        else:
            print(f"Skipping {mysql_table} (no input file)")

    print()
    print("=" * 50)
    print(f"Conversion complete! Converted {converted_count}/{len(mysql_tables)} tables.")
    print()
    print("Table name mapping:")
    for mysql_table, postgres_table in TABLE_NAME_MAPPING.items():
        print(f"  {mysql_table} → {postgres_table}")
    print()
    print("Next steps:")
    print("1. Review the *_pg.csv files")
    print("2. Run import_postgres.py to load data into PostgreSQL")


if __name__ == '__main__':
    main()
