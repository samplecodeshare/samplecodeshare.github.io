import sqlite3
import yaml
import os

def create_sqlite_table(db_name):
    # Connect to the SQLite database (or create it if it doesn't exist)
    conn = sqlite3.connect(db_name)
    cursor = conn.cursor()

    # Define the SQL command to create a table
    create_table_sql = '''
    CREATE TABLE IF NOT EXISTS schema_info (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        field TEXT NOT NULL,
        field_type TEXT NOT NULL,
        path TEXT NOT NULL
    );
    '''

    # Execute the create table command
    cursor.execute(create_table_sql)
    conn.commit()

    return conn, cursor

def insert_schema_info(cursor, field, field_type, path):
    # Define the SQL command to insert data
    insert_data_sql = 'INSERT INTO schema_info (field, field_type, path) VALUES (?, ?, ?)'
    # Execute the insert data command
    cursor.execute(insert_data_sql, (field, field_type, path))

def traverse_schema(schema, parent_path="root"):
    fields_info = []

    def recurse(properties, current_path):
        for field, details in properties.items():
            field_path = f"{current_path}/{field}"
            field_type = details.get('type', 'object')
            fields_info.append((field, field_type, field_path))
            
            if field_type == 'object' and 'properties' in details:
                recurse(details['properties'], field_path)
            elif field_type == 'array' and 'items' in details:
                item_type = details['items'].get('type', 'object')
                fields_info.append((f"{field}[]", item_type, f"{field_path}[]"))
                if item_type == 'object' and 'properties' in details['items']:
                    recurse(details['items']['properties'], f"{field_path}[]")

    if 'properties' in schema:
        recurse(schema['properties'], parent_path)
    
    return fields_info

def load_openapi_spec(file_path):
    with open(file_path, 'r') as file:
        return yaml.safe_load(file)

def main():
    db_name = 'openapi_schema.db'
    openapi_spec_path = 'openapi.yaml'  # Path to your OpenAPI spec file

    # Load the OpenAPI spec
    openapi_spec = load_openapi_spec(openapi_spec_path)
    schemas = openapi_spec.get('components', {}).get('schemas', {})

    # Create SQLite table
    conn, cursor = create_sqlite_table(db_name)

    # Extract schema information and store it in the database
    for schema_name, schema_details in schemas.items():
        fields_info = traverse_schema(schema_details)
        for field, field_type, path in fields_info:
            insert_schema_info(cursor, field, field_type, path)

    # Commit the changes and close the connection
    conn.commit()
    conn.close()

    print("Schema information extracted and stored successfully.")

if __name__ == '__main__':
    main()
