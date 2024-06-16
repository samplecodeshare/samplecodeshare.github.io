import json
from jsonschema import Draft7Validator, ValidationError, SchemaError
from ruamel.yaml import YAML

def load_yaml(file_path):
    yaml = YAML()
    yaml.preserve_quotes = True
    with open(file_path, 'r') as file:
        return yaml.load(file)

def load_json_schema(file_path):
    with open(file_path, 'r') as file:
        return json.load(file)

def get_line_number(data, path):
    # Traverse the data structure to find the node
    node = data
    for key in path:
        if isinstance(key, int):
            node = node[key]
        else:
            node = node.get(key, None)
            if node is None:
                return "unknown"
    # Return the line number if available
    if hasattr(node, 'lc') and hasattr(node.lc, 'line'):
        return node.lc.line + 1
    return "unknown"

def print_validation_error(error, yaml_content):
    # Find the path to the error
    path = list(error.path)
    line_number = get_line_number(yaml_content, path)
    print(f"Error: {error.message}")
    print(f"Location: {'.'.join(map(str, path))}")
    print(f"Line number: {line_number}\n")

def validate_yaml_with_schema(yaml_data, schema):
    validator = Draft7Validator(schema)
    errors = list(validator.iter_errors(yaml_data))
    
    if errors:
        print(f"Found {len(errors)} validation errors:\n")
        for error in errors:
            print_validation_error(error, yaml_data)
    else:
        print("YAML data is valid against the schema.")

if __name__ == "__main__":
    yaml_file_path = 'data.yaml'
    schema_file_path = 'schema.json'

    yaml_data = load_yaml(yaml_file_path)
    schema = load_json_schema(schema_file_path)

    validate_yaml_with_schema(yaml_data, schema)
