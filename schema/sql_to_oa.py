# pip install pyodbc jsonschema pyyaml

import pyodbc
import json
import yaml
from collections import OrderedDict

def connect_to_db(connection_string):
    conn = pyodbc.connect(connection_string)
    return conn

def execute_query(conn, query):
    cursor = conn.cursor()
    cursor.execute(query)
    columns = [column[0] for column in cursor.description]
    rows = cursor.fetchall()
    return columns, rows

def generate_json_schema(columns, rows):
    def get_type(value):
        if isinstance(value, int):
            return "integer"
        elif isinstance(value, float):
            return "number"
        elif isinstance(value, bool):
            return "boolean"
        elif isinstance(value, str):
            return "string"
        else:
            return "null"
    
    schema = OrderedDict({
        "type": "object",
        "properties": OrderedDict()
    })

    for column in columns:
        example_value = rows[0][columns.index(column)] if rows else None
        column_type = get_type(example_value)
        
        schema["properties"][column] = {
            "type": column_type
        }

        if column_type == "string":
            schema["properties"][column]["minLength"] = len(example_value)
            schema["properties"][column]["maxLength"] = len(example_value)
            if '@' in example_value:
                schema["properties"][column]["format"] = "email"
            if '-' in example_value and len(example_value) == 36:
                schema["properties"][column]["format"] = "uuid"
                
    return schema

def jsonschema_to_openapi(json_schema, title="API Schema", version="1.0.0"):
    openapi_template = {
        "openapi": "3.0.0",
        "info": {
            "title": title,
            "version": version
        },
        "paths": {},
        "components": {
            "schemas": {
                "Example": json_schema
            }
        }
    }
    return openapi_template

# Connection and query (Replace with your actual connection string and query)
connection_string = 'DRIVER={ODBC Driver 17 for SQL Server};SERVER=your_server;DATABASE=your_db;UID=your_user;PWD=your_password'
query = 'SELECT * FROM your_table'

# Connect to the database and execute query
conn = connect_to_db(connection_string)
columns, rows = execute_query(conn, query)

# Generate JSON Schema from the result
json_schema = generate_json_schema(columns, rows)

# Convert JSON Schema to OpenAPI schema
openapi_schema = jsonschema_to_openapi(json_schema)

# Print the OpenAPI schema in YAML format
openapi_yaml = yaml.dump(openapi_schema, sort_keys=False)
print(openapi_yaml)

# Optionally save to a file
with open("openapi_schema.yaml", "w") as file:
    file.write(openapi_yaml)
