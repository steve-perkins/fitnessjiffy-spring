#!/usr/bin/env python3
"""
Export MySQL data via SSH tunnel with UUIDs as HEX strings

Connects to remote MySQL server through SSH tunnel and exports
all tables with UUID columns converted to HEX format for easy
conversion to PostgreSQL.

Usage:
    python3 export_mysql_via_ssh.py
    python3 export_mysql_via_ssh.py --ssh-passphrase "your-passphrase"
"""

import argparse
import csv
import getpass
import sys
from pathlib import Path

try:
    import paramiko
    import pymysql
except ImportError:
    print("Required packages not installed.")
    print("Run: pip3 install paramiko pymysql")
    sys.exit(1)


MYSQL_HOST = '127.0.0.1'
MYSQL_PORT = 3306

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


def export_table(connection, table_name, uuid_columns):
    """Export a table with UUIDs converted to HEX strings"""

    print(f"Exporting {table_name}...", end=' ', flush=True)

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

        # Write to CSV
        output_file = f"{table_name}.csv"
        with open(output_file, 'w', newline='', encoding='utf-8') as csvfile:
            writer = csv.writer(csvfile)

            # Write header
            writer.writerow(columns)

            # Write data
            row_count = 0
            for row in cursor:
                # Convert None to empty string for CSV
                cleaned_row = ['' if val is None else val for val in row]
                writer.writerow(cleaned_row)
                row_count += 1

        print(f"✓ {row_count} rows")

    return row_count


def main():
    parser = argparse.ArgumentParser(description='Export MySQL data via SSH tunnel')
    parser.add_argument('--ssh-host', help='SSH host (will prompt if not provided)')
    parser.add_argument('--ssh-user', help='SSH user (will prompt if not provided)')
    parser.add_argument('--ssh-keyfile', help='SSH key file (will prompt if not provided)')
    parser.add_argument('--ssh-passphrase', help='SSH key passphrase (will prompt if not provided)')
    parser.add_argument('--mysql-user', help='MySQL user (will prompt if not provided)')
    parser.add_argument('--mysql-password', help='MySQL password (will prompt if not provided)')
    parser.add_argument('--mysql-database', help='MySQL database (will prompt if not provided)')
    args = parser.parse_args()

    def get_required_text(value, prompt, field_name):
        text = value.strip() if value is not None else input(prompt).strip()
        if not text:
            print(f"Error: {field_name} cannot be blank.")
            sys.exit(1)
        return text

    # Get SSH host
    ssh_host = get_required_text(args.ssh_host, "SSH host: ", "SSH host")

    # Get SSH user
    ssh_user = get_required_text(args.ssh_user, "SSH user: ", "SSH user")

    # Get SSH keyfile
    if args.ssh_keyfile:
        ssh_keyfile_input = args.ssh_keyfile.strip()
    else:
        ssh_keyfile_input = input("SSH keyfile: ").strip()

    if not ssh_keyfile_input:
        print("Error: SSH keyfile path cannot be blank.")
        sys.exit(1)

    ssh_keyfile = Path(ssh_keyfile_input).expanduser()

    # Get SSH passphrase
    if args.ssh_passphrase:
        ssh_passphrase = args.ssh_passphrase
    else:
        ssh_passphrase = getpass.getpass(f"SSH key passphrase for {ssh_keyfile} (hit Enter if there is no passphrase): ")

    # Get MySQL user
    mysql_user = get_required_text(args.mysql_user, "MySQL user: ", "MySQL user")

    # Get MySQL password
    if args.mysql_password:
        mysql_password = args.mysql_password
    else:
        mysql_password = getpass.getpass(f"MySQL password for {mysql_user}@{MYSQL_HOST}: ")

    # Get MySQL database
    mysql_database = get_required_text(args.mysql_database, "MySQL database: ", "MySQL database")

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
        print(f"Export complete! {total_rows} total rows exported.")
        print(f"{'='*60}\n")
        print("Next steps:")
        print("1. Run: python3 convert_mysql_to_postgres.py")
        print("2. Run: ./import_postgres.sh")

    except Exception as e:
        print(f"\n✗ Error: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == '__main__':
    main()