import pyodbc
import yaml

# Function to connect to the database and execute SQL query
def execute_sql_query(dsn, sql_query):
    connection = pyodbc.connect(dsn)
    cursor = connection.cursor()
    
    # Execute the supplied SQL query
    cursor.execute(sql_query)
    columns = [column[0] for column in cursor.description]
    column_types = [desc[1] for desc in cursor.description]
    
    cursor.close()
    connection.close()
    
    return columns, column_types

# Function to map SQL data types to OpenAPI data types
def map_sql_type_to_openapi_type(sql_type):
    type_mapping = {
        'int': 'integer',
        'varchar': 'string',
        'nvarchar': 'string',
        'text': 'string',
        'date': 'string',
        'datetime': 'string',
        'bit': 'boolean',
        'float': 'number',
        'decimal': 'number',
        'numeric': 'number'
        # Add other SQL to OpenAPI type mappings as needed
    }
    return type_mapping.get(sql_type, 'string')

# Function to generate OpenAPI specification
def generate_openapi_spec(table_name, columns, column_types):
    # Define the schema in the components section
    schema_properties = {
        columns[i]: {'type': map_sql_type_to_openapi_type(column_types[i].__name__)}
        for i in range(len(columns))
    }
    
    openapi_spec = {
        'openapi': '3.0.0',
        'info': {
            'title': f'{table_name} API',
            'version': '1.0.0'
        },
        'paths': {
            f'/{table_name}': {
                'get': {
                    'summary': f'Get list of {table_name}',
                    'responses': {
                        '200': {
                            'description': f'Successful response',
                            'content': {
                                'application/json': {
                                    'schema': {
                                        '$ref': f'#/components/schemas/{table_name}'
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        'components': {
            'schemas': {
                table_name: {
                    'type': 'object',
                    'properties': schema_properties
                }
            }
        }
    }
    return openapi_spec

# Main function to execute SQL query and generate OpenAPI spec
def main():
    dsn = 'DSN=your_dsn_name;UID=your_username;PWD=your_password'
    sql_query = 'your_sql_query'  # Replace with your SQL query
    table_name = 'your_table_name'  # Replace with your table name
    
    columns, column_types = execute_sql_query(dsn, sql_query)
    openapi_spec = generate_openapi_spec(table_name, columns, column_types)
    
    with open(f'{table_name}_openapi.yaml', 'w') as file:
        yaml.dump(openapi_spec, file, sort_keys=False)
        
    print(f'OpenAPI spec for {table_name} generated successfully!')

if __name__ == '__main__':
    main()
