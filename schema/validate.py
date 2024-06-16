import yaml
import json
from jsonschema import validate, ValidationError, SchemaError

def load_yaml(file_path):
    with open(file_path, 'r') as file:
        return yaml.safe_load(file)

def load_json_schema(file_path):
    with open(file_path, 'r') as file:
        return json.load(file)

def validate_yaml_with_schema(yaml_data, schema):
    try:
        validate(instance=yaml_data, schema=schema)
        print("YAML data is valid against the schema.")
    except ValidationError as ve:
        print(f"YAML data is not valid. Error: {ve.message}")
    except SchemaError as se:
        print(f"Schema is not valid. Error: {se.message}")

if __name__ == "__main__":
    yaml_file_path = 'data.yaml'
    schema_file_path = 'schema.json'

    yaml_data = load_yaml(yaml_file_path)
    schema = load_json_schema(schema_file_path)

    validate_yaml_with_schema(yaml_data, schema)
