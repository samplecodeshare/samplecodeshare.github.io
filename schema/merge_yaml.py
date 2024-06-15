import ruamel.yaml
import sys
import argparse

def load_yaml(file_path):
    with open(file_path, 'r') as file:
        return ruamel.yaml.safe_load(file)

def merge_and_substitute(base_yaml, params_yaml):
    # Load YAML files
    base_data = load_yaml(base_yaml)
    params_data = load_yaml(params_yaml)

    # Perform variable substitution in base_data
    substituted_data = substitute_variables(base_data, params_data)

    return substituted_data

def substitute_variables(data, params):
    if isinstance(data, dict):
        for key, value in data.items():
            if isinstance(value, str):
                data[key] = substitute_value(value, params)
            elif isinstance(value, (dict, list)):
                data[key] = substitute_variables(value, params)
    elif isinstance(data, list):
        data = [substitute_variables(item, params) for item in data]
    return data

def substitute_value(value, params):
    # Substitute variables in a single value
    if isinstance(value, str):
        for param_key, param_value in params.items():
            if isinstance(param_value, (str, int, float)):
                value = value.replace(f"${{{param_key}}}", str(param_value))
            elif isinstance(param_value, dict):
                # Recursively substitute nested variables
                for nested_key, nested_value in param_value.items():
                    nested_placeholder = f"${{{param_key}.{nested_key}}}"
                    value = value.replace(nested_placeholder, str(nested_value))
    return value

def main():
    parser = argparse.ArgumentParser(description='Transform YAML structure from models/fields to schemas/properties.')
    parser.add_argument('input_file', help='Input YAML file path', default='main.yaml')
    parser.add_argument('params_file', help='Output YAML file path', default='parameters.yaml')

    # Parse command line arguments
    args = parser.parse_args()

    merged_yaml = merge_and_substitute(args.input_file, args.params_file)
    ruamel.yaml.round_trip_dump(merged_yaml, sys.stdout, default_flow_style=False)
    
if __name__ == "__main__":
    main()
