import json
from jsonschema import validate, ValidationError, SchemaError
from ruamel.yaml import YAML

def load_yaml(file_path):
    yaml = YAML()
    with open(file_path, 'r') as file:
        return yaml.load(file)

def load_json_schema(file_path):
    with open(file_path, 'r') as file:
        return json.load(file)

def print_validation_error(error, yaml_content):
    # Find the path to the error
    path = list(error.path)
    if path:
        error_location = yaml_content
        for key in path:
            if isinstance(key, int):
                error_location = error_location[key]
            else:
                error_location = error_location.get(key, None)
                if error_location is None:
                    break
        # Attempt to get the line number
        line_number = error_location.lc.line + 1 if hasattr(error_location, 'lc') else "unknown"
    else:
        line_number = "unknown"

    print(f"Error: {error.message}")
    print(f"Location: {'.'.join(map(str, path))}")
    print(f"Line number: {line_number}")

def validate_yaml_with_schema(yaml_data, schema):
    try:
        validate(instance=yaml_data, schema=schema)
        print("YAML data is valid against the schema.")
    except ValidationError as ve:
        print("YAML data is not valid.")
        print_validation_error(ve, yaml_data)
    except SchemaError as se:
        print(f"Schema is not valid. Error: {se.message}")

if __name__ == "__main__":
    yaml_file_path = 'data.yaml'
    schema_file_path = 'schema.json'

    yaml_data = load_yaml(yaml_file_path)
    schema = load_json_schema(schema_file_path)

    validate_yaml_with_schema(yaml_data, schema)
