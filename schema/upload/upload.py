import os
import requests
import yaml

# Function to search for YAML files recursively in a directory
def find_yaml_files(directory):
    yaml_files = []
    for root, _, files in os.walk(directory):
        for file in files:
            if file.endswith(".yaml") or file.endswith(".yml"):
                yaml_files.append(os.path.join(root, file))
    return yaml_files

# Function to upload a YAML file to the REST API
def upload_yaml_file(file_path, url):
    with open(file_path, 'r') as file:
        data = yaml.safe_load(file)
        response = requests.post(url, json=data, headers={'Content-Type': 'application/json'})
        if response.status_code == 201:
            print(f"Successfully uploaded {file_path}")
        else:
            print(f"Failed to upload {file_path}: {response.status_code} {response.text}")

# Main function
def main():
    directory = 'path/to/your/directory'  # Change this to your directory path
    api_url = 'http://localhost:8080/contracts'  # Change this to your API endpoint

    yaml_files = find_yaml_files(directory)
    for yaml_file in yaml_files:
        upload_yaml_file(yaml_file, api_url)

if __name__ == "__main__":
    main()
