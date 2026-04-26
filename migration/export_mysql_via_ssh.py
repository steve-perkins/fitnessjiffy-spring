#!/usr/bin/env python3
"""
Export MySQL data via SSH tunnel and convert to PostgreSQL format

Connects to remote MySQL server through SSH tunnel and exports
all tables, producing both raw CSV files and PostgreSQL-ready
CSV files with proper UUID format, column renaming, and table
name mapping.

Usage:
    python3 export_mysql_via_ssh.py
    python3 export_mysql_via_ssh.py --ssh-passphrase "your-passphrase"
"""

import argparse
import csv
import getpass
import os
import sys
import uuid
from datetime import datetime
from pathlib import Path

try:
    import paramiko
    import pymysql
    from dotenv import load_dotenv
except ImportError:
    print("Required packages not installed.")
    print("Run: pip3 install paramiko pymysql python-dotenv")
    sys.exit(1)

# Load .env file from current directory (if it exists)
load_dotenv()


MYSQL_HOST = '127.0.0.1'
MYSQL_PORT = 3306

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

# Tables and their UUID columns
TABLES_WITH_UUIDS = {
    'fitnessjiffy_user': ['id'],
    'food': ['id', 'owner_id'],
    'food_eaten': ['id', 'user_id', 'food_id'],
    'exercise': ['id'],
    'exercise_performed': ['id', 'user_id', 'exercise_id'],
    'weight': ['id', 'user_id'],
    'report_data': ['id', 'user_id'],
}

# Timestamp columns (across all tables)
TIMESTAMP_COLUMNS = {'created_time', 'last_updated_time'}


def binary_to_uuid(hex_str):
    """Convert MySQL HEX UUID to standard UUID format

    The HEX() function gives us 32-character hex strings. We convert these
    to standard UUID format with hyphens.
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


def convert_timestamp(timestamp_value):
    """Convert MySQL TIMESTAMP to PostgreSQL TIMESTAMP WITH TIME ZONE format"""
    if not timestamp_value or timestamp_value in ('NULL', '\\N', ''):
        return None

    # If it's already a datetime object (from PyMySQL cursor), convert directly
    if isinstance(timestamp_value, datetime):
        return timestamp_value.isoformat()

    # If it's a string, parse it first
    try:
        # MySQL format: YYYY-MM-DD HH:MM:SS
        dt = datetime.strptime(timestamp_value, '%Y-%m-%d %H:%M:%S')
        # PostgreSQL format (ISO 8601)
        return dt.isoformat()
    except ValueError:
        # Try alternative format
        try:
            dt = datetime.fromisoformat(timestamp_value)
            return dt.isoformat()
        except:
            print(f"Warning: Could not parse timestamp: {timestamp_value}", file=sys.stderr)
            return timestamp_value


def export_table(connection, table_name, uuid_columns):
    """Export a table with UUIDs converted to HEX strings, producing both raw and PostgreSQL-ready CSVs"""

    postgres_table_name = TABLE_NAME_MAPPING[table_name]
    print(f"Exporting {table_name} → {postgres_table_name}...", end=' ', flush=True)

    with connection.cursor() as cursor:
        # Get column names
        cursor.execute(f"SHOW COLUMNS FROM {table_name}")
        columns = [row[0] for row in cursor.fetchall()]

        # Build SELECT with HEX() for UUID columns
        select_parts = []
        for col in columns:
            if col in uuid_columns:
                select_parts.append(f"HEX({col}) as {col}")
            else:
                select_parts.append(col)

        select_sql = f"SELECT {', '.join(select_parts)} FROM {table_name}"

        # Execute query
        cursor.execute(select_sql)

        # Prepare column names for PostgreSQL output
        pg_columns = list(columns)

        # Special handling for fitnessjiffy_user: rename gender → sex
        if table_name == 'fitnessjiffy_user':
            if 'gender' in pg_columns:
                pg_columns[pg_columns.index('gender')] = 'sex'

        # Special handling for report_data: remove net_points
        if table_name == 'report_data':
            if 'net_points' in pg_columns:
                pg_columns.remove('net_points')

        # Write both CSV files
        raw_output_file = f"{table_name}.csv"
        pg_output_file = f"{postgres_table_name}_pg.csv"

        with open(raw_output_file, 'w', newline='', encoding='utf-8') as raw_csvfile, \
             open(pg_output_file, 'w', newline='', encoding='utf-8') as pg_csvfile:

            # Raw CSV writer
            raw_writer = csv.writer(raw_csvfile)
            raw_writer.writerow(columns)

            # PostgreSQL CSV writer
            pg_writer = csv.DictWriter(pg_csvfile, fieldnames=pg_columns)
            pg_writer.writeheader()

            # Write data
            row_count = 0
            for row in cursor:
                # Write raw CSV
                cleaned_row = ['' if val is None else val for val in row]
                raw_writer.writerow(cleaned_row)

                # Convert for PostgreSQL CSV
                pg_row = {}
                for i, col in enumerate(columns):
                    # Handle renamed column
                    pg_col = 'sex' if col == 'gender' and table_name == 'fitnessjiffy_user' else col

                    # Skip net_points if it's report_data
                    if col == 'net_points' and table_name == 'report_data':
                        continue

                    value = row[i]

                    # Convert empty strings to None for processing
                    if value == '':
                        value = None

                    # Convert UUIDs
                    if col in uuid_columns:
                        value = binary_to_uuid(value)

                    # Convert timestamps
                    elif col in TIMESTAMP_COLUMNS:
                        value = convert_timestamp(value)

                    # Handle NULL values
                    if value in ('NULL', '\\N', None):
                        value = ''  # PostgreSQL COPY treats empty as NULL

                    pg_row[pg_col] = value if value is not None else ''

                pg_writer.writerow(pg_row)
                row_count += 1

        print(f"✓ {row_count} rows")

    return row_count


def main():
    parser = argparse.ArgumentParser(description='Export MySQL data via SSH tunnel')
    parser.add_argument('--ssh-host', help='SSH host (env: SSH_HOST)')
    parser.add_argument('--ssh-user', help='SSH user (env: SSH_USER)')
    parser.add_argument('--ssh-keyfile', help='SSH key file (env: SSH_KEYFILE)')
    parser.add_argument('--ssh-passphrase', help='SSH key passphrase (env: SSH_PASSPHRASE)')
    parser.add_argument('--mysql-user', help='MySQL user (env: MYSQL_USER)')
    parser.add_argument('--mysql-password', help='MySQL password (env: MYSQL_PASSWORD)')
    parser.add_argument('--mysql-database', help='MySQL database (env: MYSQL_DATABASE)')
    args = parser.parse_args()

    def get_value(arg_value, env_var, prompt, required=True, is_password=False):
        """Get value from: CLI arg -> env var -> user prompt (in that order)"""
        # First check CLI argument
        if arg_value is not None:
            value = arg_value.strip()
            if value:
                return value

        # Then check environment variable
        env_value = os.environ.get(env_var)
        if env_value is not None:
            value = env_value.strip()
            if value:
                return value

        # Finally, prompt the user
        if is_password:
            value = getpass.getpass(prompt)
        else:
            value = input(prompt).strip()

        if required and not value:
            print(f"Error: {env_var} cannot be blank.")
            sys.exit(1)

        return value

    # Get SSH host
    ssh_host = get_value(args.ssh_host, 'SSH_HOST', "SSH host: ")

    # Get SSH user
    ssh_user = get_value(args.ssh_user, 'SSH_USER', "SSH user: ")

    # Get SSH keyfile
    ssh_keyfile_input = get_value(args.ssh_keyfile, 'SSH_KEYFILE', "SSH keyfile: ")
    ssh_keyfile = Path(ssh_keyfile_input).expanduser()

    # Get SSH passphrase (not required - key may not have passphrase)
    ssh_passphrase = get_value(
        args.ssh_passphrase,
        'SSH_PASSPHRASE',
        f"SSH key passphrase for {ssh_keyfile} (hit Enter if there is no passphrase): ",
        required=False,
        is_password=True
    )

    # Get MySQL user
    mysql_user = get_value(args.mysql_user, 'MYSQL_USER', "MySQL user: ")

    # Get MySQL password
    mysql_password = get_value(
        args.mysql_password,
        'MYSQL_PASSWORD',
        f"MySQL password for {mysql_user}@{MYSQL_HOST}: ",
        is_password=True
    )

    # Get MySQL database
    mysql_database = get_value(args.mysql_database, 'MYSQL_DATABASE', "MySQL database: ")

    # Verify SSH key exists
    if not ssh_keyfile.exists():
        print(f"Error: SSH key not found at {ssh_keyfile}")
        sys.exit(1)

    print(f"\n{'='*60}")
    print("MySQL Data Export via SSH Tunnel")
    print(f"{'='*60}\n")
    print(f"SSH: {ssh_user}@{ssh_host}")
    print(f"MySQL: {mysql_user}@{MYSQL_HOST}:{MYSQL_PORT}/{mysql_database}")
    print()

    try:
        # Load SSH private key
        print("Loading SSH key...", flush=True)
        try:
            # Try different key types
            for key_class in [paramiko.RSAKey, paramiko.Ed25519Key, paramiko.ECDSAKey]:
                try:
                    pkey = key_class.from_private_key_file(
                        str(ssh_keyfile),
                        password=ssh_passphrase
                    )
                    print(f"✓ Loaded {key_class.__name__}\n")
                    break
                except paramiko.SSHException:
                    continue
            else:
                raise Exception("Could not load SSH key (unsupported key type)")
        except Exception as e:
            raise Exception(f"Failed to load SSH key: {e}")

        # Create SSH client
        print("Connecting to SSH server...", flush=True)
        ssh_client = paramiko.SSHClient()
        ssh_client.set_missing_host_key_policy(paramiko.AutoAddPolicy())

        ssh_client.connect(
            hostname=ssh_host,
            port=22,
            username=ssh_user,
            pkey=pkey,
            look_for_keys=False,
            allow_agent=False,
        )
        print(f"✓ Connected to {ssh_host}\n")

        # Create port forward (SSH tunnel)
        print("Creating MySQL tunnel...", flush=True)
        transport = ssh_client.get_transport()

        # Request port forwarding
        # This creates a tunnel from localhost:local_port -> remote:MYSQL_PORT
        local_port = 13306  # Use a non-standard local port

        # Open a direct TCP/IP channel through SSH
        # This is like: ssh -L local_port:MYSQL_HOST:MYSQL_PORT
        channel = transport.open_channel(
            "direct-tcpip",
            (MYSQL_HOST, MYSQL_PORT),
            ("127.0.0.1", local_port),
        )

        print(f"✓ Tunnel created to {MYSQL_HOST}:{MYSQL_PORT}\n")

        # Connect to MySQL through tunnel using the channel as a socket
        print("Connecting to MySQL...", flush=True)

        # We need to wrap the channel to work like a socket for pymysql
        # Use pymysql's ability to accept a custom socket
        import socket

        # Create a socket-like wrapper for the paramiko channel
        class ChannelFile:
            def __init__(self, channel):
                self.channel = channel

            def recv(self, size):
                return self.channel.recv(size)

            def send(self, data):
                return self.channel.send(data)

            def sendall(self, data):
                self.channel.sendall(data)

            def close(self):
                self.channel.close()

            def settimeout(self, timeout):
                self.channel.settimeout(timeout)

        # Actually, let's use a simpler approach with local socket forwarding
        # Close this channel and use paramiko's forward_tunnel pattern
        channel.close()

        # Better approach: Use local socket and forward through SSH
        import threading
        import socketserver

        class ForwardServer(socketserver.ThreadingTCPServer):
            daemon_threads = True
            allow_reuse_address = True

        class Handler(socketserver.BaseRequestHandler):
            def handle(self):
                try:
                    chan = transport.open_channel(
                        "direct-tcpip",
                        (MYSQL_HOST, MYSQL_PORT),
                        self.request.getpeername(),
                    )
                except Exception as e:
                    print(f"Forwarding request failed: {e}")
                    return

                if chan is None:
                    print("Forwarding request denied")
                    return

                # Forward data between local socket and SSH channel
                try:
                    while True:
                        import select
                        r, w, x = select.select([self.request, chan], [], [], 1)
                        if self.request in r:
                            data = self.request.recv(1024)
                            if len(data) == 0:
                                break
                            chan.send(data)
                        if chan in r:
                            data = chan.recv(1024)
                            if len(data) == 0:
                                break
                            self.request.send(data)
                except Exception:
                    pass
                finally:
                    chan.close()
                    self.request.close()

        # Start forwarding server in background thread
        forward_server = ForwardServer(("127.0.0.1", local_port), Handler)
        forward_thread = threading.Thread(target=forward_server.serve_forever)
        forward_thread.daemon = True
        forward_thread.start()

        print(f"✓ Port forwarding active on localhost:{local_port}\n")

        # Now connect to MySQL through the tunnel
        print("Connecting to MySQL...", flush=True)
        connection = pymysql.connect(
            host='127.0.0.1',
            port=local_port,
            user=mysql_user,
            password=mysql_password,
            database=mysql_database,
            charset='utf8mb4',
        )
        print("✓ Connected to MySQL\n")

        # Export tables
        total_rows = 0
        for table_name, uuid_columns in TABLES_WITH_UUIDS.items():
            row_count = export_table(connection, table_name, uuid_columns)
            total_rows += row_count

        # Cleanup
        connection.close()
        forward_server.shutdown()
        ssh_client.close()

        print(f"\n{'='*60}")
        print(f"Export and conversion complete! {total_rows} total rows exported.")
        print(f"{'='*60}\n")
        print("Generated files:")
        print("  Raw MySQL CSVs: <table>.csv")
        print("  PostgreSQL-ready CSVs: <table>_pg.csv")
        print()
        print("Next step:")
        print("  Run: python3 import_postgres.py")

    except Exception as e:
        print(f"\n✗ Error: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == '__main__':
    main()