import os
import sqlite3
import yaml

def create_sqlite_table(db_name):
    # Connect to the SQLite database (or create it if it doesn't exist)
    conn = sqlite3.connect(db_name)
    cursor = conn.cursor()

    # Define the SQL command to create a table
    create_table_sql = '''
    CREATE TABLE IF NOT EXISTS schema_info (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        domain TEXT NOT NULL,
        table_name TEXT NOT NULL,
        field TEXT NOT NULL,
        field_type TEXT NOT NULL,
        path TEXT NOT NULL
    );
    '''

    # Execute the create table command
    cursor.execute(create_table_sql)
    conn.commit()

    return conn, cursor

def insert_schema_info(cursor, domain, table_name, field, field_type, path):
    # Define the SQL command to insert data
    insert_data_sql = 'INSERT INTO schema_info (domain, table_name, field, field_type, path) VALUES (?, ?, ?, ?, ?)'
    # Execute the insert data command
    cursor.execute(insert_data_sql, (domain, table_name, field, field_type, path))

def traverse_schema(schema, components, parent_path="root"):
    fields_info = []

    def recurse(properties, current_path):
        for field, details in properties.items():
            field_path = f"{current_path}/{field}"
            if '$ref' in details:
                ref_path = details['$ref']
                fields_info.append((field, 'ref', f"{current_path}{ref_path}"))
            else:
                field_type = details.get('type', 'object')
                fields_info.append((field, field_type, field_path))

                if field_type == 'object' and 'properties' in details:
                    recurse(details['properties'], field_path)
                elif field_type == 'array' and 'items' in details:
                    item_details = details['items']
                    if '$ref' in item_details:
                        ref_path = item_details['$ref']
                        fields_info.append((f"{field}[]", 'ref', f"{current_path}{ref_path}"))
                    else:
                        item_type = item_details.get('type', 'object')
                        fields_info.append((f"{field}[]", item_type, f"{field_path}[]"))
                        if item_type == 'object' and 'properties' in item_details:
                            recurse(item_details['properties'], f"{field_path}[]")

    if 'properties' in schema:
        recurse(schema['properties'], parent_path)
    elif 'items' in schema and '$ref' in schema['items']:
        ref_path = schema['items']['$ref']
        fields_info.append((parent_path, 'ref', f"{parent_path}{ref_path}"))
    
    return fields_info

def load_openapi_spec(file_path):
    with open(file_path, 'r') as file:
        return yaml.safe_load(file)

def process_yaml_file(file_path, cursor):
    print("Extracting schema for file=" + file_path)
    # Load the OpenAPI spec
    openapi_spec = load_openapi_spec(file_path)
    
    # Extract domain and table name from the `info` tag
    info = openapi_spec.get('info', {})
    domain = info.get('domain', 'unknown_domain')
    table_name = info.get('title', 'unknown_table')
    
    components = openapi_spec.get('components', {}).get('schemas', {})

    # Extract schema information and store it in the database
    for schema_name, schema_details in components.items():
        fields_info = traverse_schema(schema_details, components, schema_name)
        for field, field_type, path in fields_info:
            insert_schema_info(cursor, domain, table_name, field, field_type, path)

def traverse_directory(directory, cursor):
    for root, _, files in os.walk(directory):
        for file in files:
            if file.endswith('.yaml'):
                file_path = os.path.join(root, file)
                process_yaml_file(file_path, cursor)

def main():
    db_name = 'openapi_schema.db'
    directory = '.'  # Directory containing the YAML files

    # Create SQLite table
    conn, cursor = create_sqlite_table(db_name)

    # Traverse the directory and process each YAML file
    traverse_directory(directory, cursor)

    # Commit the changes and close the connection
    conn.commit()
    conn.close()

    print("Schema information extracted and stored successfully.")

if __name__ == '__main__':
    main()
