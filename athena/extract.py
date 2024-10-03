import csv
import psycopg2
from psycopg2 import sql
import configparser
from cryptography.fernet import Fernet
import sys

def fetch_and_write_to_csv(query, csv_file_path, db_config, chunk_size=10000):
    """
    Executes a SQL query and streams the result row by row to a CSV file.

    :param query: SQL query to execute.
    :param csv_file_path: File path to save the CSV.
    :param db_config: Dictionary containing database connection parameters.
    :param chunk_size: Number of rows to fetch per iteration (for cursor chunking).
    """
    try:
        # Establish database connection
        connection = psycopg2.connect(**db_config)
        cursor = connection.cursor()

        # Execute the query
        cursor.execute(sql.SQL(query))

        # Open the CSV file to write the data
        with open(csv_file_path, mode='w', newline='', encoding='utf-8') as csv_file:
            csv_writer = csv.writer(csv_file)

            # Fetch and write column headers first
            headers = [desc[0] for desc in cursor.description]
            csv_writer.writerow(headers)

            # Fetch and write rows in chunks
            while True:
                rows = cursor.fetchmany(chunk_size)
                if not rows:
                    break  # Exit when no more rows are returned
                csv_writer.writerows(rows)

        print(f"Data successfully written to {csv_file_path}")

    except Exception as e:
        print(f"Error occurred: {e}")

    finally:
        if cursor:
            cursor.close()
        if connection:
            connection.close()


def get_db_config_and_query(config_file, section):
    """
    Reads the INI configuration file to get the database configuration and SQL query.

    :param config_file: Path to the configuration file.
    :param section: The section in the INI file to get the SQL query from.
    :return: Tuple of (db_config dict, sql query string).
    """
    config = configparser.ConfigParser()
    config.read(config_file)

    # Get the encrypted password and other database configs from the 'global' section
    dbname = config.get('global', 'dbname')
    user = config.get('global', 'user')
    encrypted_password = config.get('global', 'password')
    host = config.get('global', 'host')
    port = config.get('global', 'port')

    # Load the encryption key from the file
    with open('secret.key', 'rb') as key_file:
        key = key_file.read()

    # Create a Fernet instance
    cipher_suite = Fernet(key)

    # Decrypt the password
    decrypted_password = cipher_suite.decrypt(encrypted_password.encode()).decode()

    # Prepare the DB config dictionary
    db_config = {
        'dbname': dbname,
        'user': user,
        'password': decrypted_password,
        'host': host,
        'port': port
    }

    # Get the SQL query from the specified section
    query = config.get(section, 'sql')

    return db_config, query


if __name__ == '__main__':
    if len(sys.argv) != 3:
        print("Usage: python script.py <config_file> <section_name>")
        sys.exit(1)

    config_file = sys.argv[1]  # Path to the config file
    section_name = sys.argv[2]  # Section name to get the SQL query from

    # Get the DB config and query from the INI file
    db_config, query = get_db_config_and_query(config_file, section_name)

    # Path to the CSV file where data will be written
    csv_file_path = f'{section_name}_output.csv'

    # Fetch data and write to CSV
    fetch_and_write_to_csv(query, csv_file_path, db_config)




# [global]
# dbname = your_database
# user = your_user
# password = gAAAAABf2U2_VIICSTAb6a9BO8G85...
# host = localhost
# port = 5432

# [query1]
# sql = SELECT * FROM table1 WHERE some_condition = true
