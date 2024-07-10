import requests
import yaml
import os

# Function to extract URLs from the YAML file
def extract_urls_from_yaml(yaml_content):
    urls = []

    def extract(value):
        if isinstance(value, dict):
            for k, v in value.items():
                if k == 'ref' and isinstance(v, str) and v.startswith('$'):
                    url = v.split('$')[-1].split('/')[0]  # Extract the URL part
                    urls.append(url)
                else:
                    extract(v)
        elif isinstance(value, list):
            for item in value:
                extract(item)

    extract(yaml_content)
    return urls

# Function to make API calls and save responses to files
def fetch_and_save_responses(urls, output_dir):
    os.makedirs(output_dir, exist_ok=True)
    file_paths = []

    for i, url in enumerate(urls):
        response = requests.get(url)
        if response.status_code == 200:
            file_path = os.path.join(output_dir, f'response_{i}.txt')
            with open(file_path, 'w') as file:
                file.write(response.text)
            file_paths.append(file_path)
        else:
            print(f"Failed to fetch {url}: Status code {response.status_code}")

    return file_paths

# Main function
def main(yaml_file, output_dir):
    with open(yaml_file, 'r') as file:
        yaml_content = yaml.safe_load(file)

    urls = extract_urls_from_yaml(yaml_content)
    file_paths = fetch_and_save_responses(urls, output_dir)
    print(f"Files have been written to directory: {output_dir}")

if __name__ == "__main__":
    yaml_file = 'input.yaml'        # Path to your YAML file
    output_dir = 'output_files'     # Directory to save response files

    main(yaml_file, output_dir)
