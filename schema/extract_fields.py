import yaml
from sqlalchemy import create_engine, MetaData, Table, Column, String
from sqlalchemy.orm import sessionmaker

# Load configuration from YAML file
def load_config(config_path):
    with open(config_path, 'r') as file:
        config = yaml.safe_load(file)
    return config

# Load OpenAPI specification from YAML file
def load_openapi_spec(openapi_path):
    with open(openapi_path, 'r') as file:
        openapi_spec = yaml.safe_load(file)
    return openapi_spec

# Extract elements from OpenAPI specification
def extract_elements(openapi_spec, base_path=''):
    elements = []

    def traverse(schema, path_prefix):
        for field, field_details in schema.get('properties', {}).items():
            full_path = f"{path_prefix}/{field}"
            elements.append({
                'domain': openapi_spec['info']['title'],
                'datafeed': base_path,
                'field': field,
                'description': field_details.get('description', ''),
                'type': field_details.get('type', ''),
                'path': full_path
            })
            if field_details.get('type') == 'object':
                traverse(field_details, full_path)

    for path, methods in openapi_spec.get('paths', {}).items():
        for method, details in methods.items():
            responses = details.get('responses', {})
            for response_code, response_details in responses.items():
                content = response_details.get('content', {})
                for media_type, media_details in content.items():
                    schema = media_details.get('schema', {})
                    if '$ref' in schema:
                        ref = schema['$ref']
                        ref_schema = ref.split('/')[-1]
                        traverse(openapi_spec['components']['schemas'][ref_schema], path)

    return elements

# Insert elements into SQL table
def insert_elements_to_db(engine, elements):
    metadata = MetaData()

    table = Table('api_spec', metadata,
                  Column('domain', String),
                  Column('datafeed', String),
                  Column('field', String),
                  Column('description', String),
                  Column('type', String),
                  Column('path', String))

    metadata.create_all(engine)

    Session = sessionmaker(bind=engine)
    session = Session()

    for element in elements:
        insert_stmt = table.insert().values(
            domain=element['domain'],
            datafeed=element['datafeed'],
            field=element['field'],
            description=element['description'],
            type=element['type'],
            path=element['path']
        )
        session.execute(insert_stmt)
    
    session.commit()
    session.close()

# Main function
def main():
    config_path = 'config.yaml'  # Path to your configuration file
    openapi_path = 'openapi.yaml'  # Path to your OpenAPI specification file

    config = load_config(config_path)
    openapi_spec = load_openapi_spec(openapi_path)

    db_type = config['database']['db_type']
    dsn = config['database']['dsn']

    engine = create_engine(dsn)

    elements = extract_elements(openapi_spec)
    insert_elements_to_db(engine, elements)

    print('Elements extracted and inserted into database successfully!')

if __name__ == '__main__':
    main()
