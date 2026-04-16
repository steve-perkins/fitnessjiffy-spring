#!/bin/bash

# Import converted CSV files into PostgreSQL
# Run this after convert_mysql_to_postgres.py

set -e  # Exit on error

# PostgreSQL connection settings
PGHOST="${PGHOST:-localhost}"
PGPORT="${PGPORT:-5432}"
PGDATABASE="${PGDATABASE:-fitness_tracker}"
PGUSER="${PGUSER:-fitness_user}"

echo "=========================================="
echo "PostgreSQL Data Import"
echo "=========================================="
echo ""
echo "Database: $PGDATABASE"
echo "Host: $PGHOST:$PGPORT"
echo "User: $PGUSER"
echo ""

# Check if PostgreSQL is accessible
if ! psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" -c '\q' 2>/dev/null; then
    echo "Error: Cannot connect to PostgreSQL"
    echo ""
    echo "Please ensure:"
    echo "  1. PostgreSQL is running (docker-compose up -d)"
    echo "  2. Database '$PGDATABASE' exists"
    echo "  3. Password is set in PGPASSWORD environment variable"
    echo ""
    echo "Example:"
    echo "  export PGPASSWORD='your_password'"
    echo "  ./import_postgres.sh"
    exit 1
fi

# Import tables in dependency order
TABLES=(
    "fitnessjiffy_user"
    "exercise"
    "food"
    "weight"
    "food_eaten"
    "exercise_performed"
    "report_data"
)

echo "Starting import..."
echo ""

for TABLE in "${TABLES[@]}"; do
    CSV_FILE="${TABLE}_pg.csv"

    if [ ! -f "$CSV_FILE" ]; then
        echo "⚠ Warning: $CSV_FILE not found, skipping..."
        continue
    fi

    echo "Importing $TABLE..."

    # Use \COPY to import from CSV (works with header row)
    psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" \
        -c "\COPY $TABLE FROM '$CSV_FILE' WITH (FORMAT CSV, HEADER true, NULL '')"

    if [ $? -eq 0 ]; then
        # Count rows
        COUNT=$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" \
            -t -c "SELECT COUNT(*) FROM $TABLE")
        echo "  ✓ Imported $COUNT rows"
    else
        echo "  ✗ Import failed"
        exit 1
    fi

    echo ""
done

echo "=========================================="
echo "Import complete!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "  1. Run validation queries (see README.md)"
echo "  2. Update user email to Gmail"
echo "  3. Verify data integrity"
