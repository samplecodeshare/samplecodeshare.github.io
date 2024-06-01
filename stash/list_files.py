import requests
from requests.auth import HTTPBasicAuth
import os

# Define your Stash server details
STASH_BASE_URL = 'https://your-stash-server-url/rest/api/1.0'
PROJECT_KEY = 'your_project_key'
REPO_SLUG = 'your_repo_slug'
USERNAME = 'your_username'
PASSWORD = 'your_password'

def get_files_recursively(project_key, repo_slug, path=''):
    files = []
    url = f"{STASH_BASE_URL}/projects/{project_key}/repos/{repo_slug}/files/{path}"
    auth = HTTPBasicAuth(USERNAME, PASSWORD)
    
    while url:
        response = requests.get(url, auth=auth)
        response.raise_for_status()
        data = response.json()
        
        for item in data.get('values', []):
            if item.endswith('/'):
                # It's a directory, recurse into it
                subdir_files = get_files_recursively(project_key, repo_slug, os.path.join(path, item))
                files.extend(subdir_files)
            elif item.endswith('.py'):
                # It's a .py file, add it to the list
                files.append(os.path.join(path, item))
        
        url = data.get('nextPageStart')

    return files

if __name__ == "__main__":
    py_files = get_files_recursively(PROJECT_KEY, REPO_SLUG)
    for file in py_files:
        print(file)
