# pip install jsonschema pyyaml
import json
import jsonschema
import yaml
from collections import OrderedDict

def json_to_jsonschema(json_obj):
    def create_schema(obj):
        if isinstance(obj, dict):
            properties = OrderedDict()
            required = []
            for k, v in obj.items():
                properties[k] = create_schema(v)
                required.append(k)
            return {
                "type": "object",
                "properties": properties,
                "required": required
            }
        elif isinstance(obj, list):
            return {
                "type": "array",
                "items": create_schema(obj[0]) if obj else {}
            }
        elif isinstance(obj, str):
            schema = {"type": "string"}
            # Example pattern for email (modify or extend patterns as needed)
            if '@' in obj:
                schema["format"] = "email"
            # Example pattern for UUID (modify or extend patterns as needed)
            if '-' in obj and len(obj) == 36:
                schema["format"] = "uuid"
            schema["minLength"] = len(obj)
            schema["maxLength"] = len(obj)
            return schema
        elif isinstance(obj, int):
            return {"type": "integer"}
        elif isinstance(obj, float):
            return {"type": "number"}
        elif isinstance(obj, bool):
            return {"type": "boolean"}
        else:
            return {"type": "null"}

    return create_schema(json_obj)

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

# Read JSON object from file
with open("example.json", "r") as file:
    example_json = json.load(file)

# Convert JSON to JSON Schema
json_schema = json_to_jsonschema(example_json)

# Convert JSON Schema to OpenAPI schema
openapi_schema = jsonschema_to_openapi(json_schema)

# Print the OpenAPI schema in YAML format
openapi_yaml = yaml.dump(openapi_schema, sort_keys=False)
print(openapi_yaml)

# Optionally save to a file
with open("openapi_schema.yaml", "w") as file:
    file.write(openapi_yaml)
