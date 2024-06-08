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

# Function to generate OpenAPI specification from template
def generate_openapi_spec_from_template(template_path, table_name, columns, column_types):
    with open(template_path, 'r') as file:
        openapi_spec = yaml.safe_load(file)
    
    # Define the schema properties
    schema_properties = {
        columns[i]: {'type': map_sql_type_to_openapi_type(column_types[i].__name__)}
        for i in range(len(columns))
    }
    
    # Update the OpenAPI spec with dynamic values
    openapi_spec['info']['title'] = openapi_spec['info']['title'].format(table_name=table_name)
    openapi_spec['paths'][f'/{table_name}'] = openapi_spec['paths']['/{table_name}']
    openapi_spec['components']['schemas'][table_name]['properties'] = schema_properties

    # Remove the placeholder in the paths
    del openapi_spec['paths']['/{table_name}']
    
    return openapi_spec

# Main function to execute SQL queries and generate OpenAPI specs
def main():
    # Load configuration from YAML file
    config_path = 'config.yaml'  # Path to your configuration file
    template_path = 'openapi_template.yaml'  # Path to your OpenAPI template
    
    with open(config_path, 'r') as file:
        config = yaml.safe_load(file)
    
    for query in config['queries']:
        table_name = query['table_name']
        dsn = query['dsn']
        sql_query = query['sql_query']
        
        columns, column_types = execute_sql_query(dsn, sql_query)
        openapi_spec = generate_openapi_spec_from_template(template_path, table_name, columns, column_types)
        
        output_file = f'{table_name}_openapi.yaml'
        with open(output_file, 'w') as file:
            yaml.dump(openapi_spec, file, sort_keys=False)
            
        print(f'OpenAPI spec for {table_name} generated successfully and saved to {output_file}!')

if __name__ == '__main__':
    main()
