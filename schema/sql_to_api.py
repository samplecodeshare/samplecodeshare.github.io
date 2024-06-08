import yaml
import pyodbc
import sqlalchemy
from sqlalchemy import create_engine

# Function to load configuration files
def load_configs(config_path, passwords_path):
    with open(config_path, 'r') as file:
        config = yaml.safe_load(file)
    with open(passwords_path, 'r') as file:
        passwords = yaml.safe_load(file)
    return config, passwords

# Function to connect to the database and execute SQL query
def execute_sql_query(db_type, dsn, password, sql_query):
    if db_type == 'mssql':
        connection_string = f"{dsn};PWD={password}"
        connection = pyodbc.connect(connection_string)
    else:
        dsn_with_password = dsn.replace("@", f":{password}@", 1)
        engine = create_engine(dsn_with_password)
        connection = engine.connect()

    result = connection.execute(sql_query)
    columns = result.keys()
    column_types = [result.cursor.description[i][1] for i in range(len(columns))]

    connection.close()
    
    return columns, column_types

# Function to map SQL data types to OpenAPI data types
def map_sql_type_to_openapi_type(db_type, sql_type):
    type_mapping = {
        'mssql': {
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
        },
        'mysql': {
            'INTEGER': 'integer',
            'VARCHAR': 'string',
            'TEXT': 'string',
            'DATE': 'string',
            'DATETIME': 'string',
            'BOOLEAN': 'boolean',
            'FLOAT': 'number',
            'DECIMAL': 'number'
        },
        'postgresql': {
            'INTEGER': 'integer',
            'VARCHAR': 'string',
            'TEXT': 'string',
            'DATE': 'string',
            'TIMESTAMP': 'string',
            'BOOLEAN': 'boolean',
            'FLOAT': 'number',
            'NUMERIC': 'number'
        }
    }
    return type_mapping[db_type].get(sql_type.upper(), 'string')

# Function to generate OpenAPI specification from template
def generate_openapi_spec_from_template(template_path, table_name, columns, column_types, db_type):
    with open(template_path, 'r') as file:
        openapi_spec = yaml.safe_load(file)
    
    # Define the schema properties
    schema_properties = {
        columns[i]: {'type': map_sql_type_to_openapi_type(db_type, column_types[i].__name__)}
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
    # Paths to configuration files
    config_path = 'config.yaml'
    passwords_path = 'passwords.yaml'
    template_path = 'openapi_template.yaml'
    
    # Load configurations
    config, passwords = load_configs(config_path, passwords_path)
    
    for db in config['databases']:
        db_name = db['name']
        db_type = db['db_type']
        dsn = db['dsn']
        password = passwords['passwords'][db_name]
        
        for query in db['queries']:
            table_name = query['table_name']
            sql_query = query['sql_query']
            
            columns, column_types = execute_sql_query(db_type, dsn, password, sql_query)
            openapi_spec = generate_openapi_spec_from_template(template_path, table_name, columns, column_types, db_type)
            
            output_file = f'{table_name}_openapi.yaml'
            with open(output_file, 'w') as file:
                yaml.dump(openapi_spec, file, sort_keys=False)
                
            print(f'OpenAPI spec for {table_name} generated successfully and saved to {output_file}!')

if __name__ == '__main__':
    main()
