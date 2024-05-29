import requests
import pandas as pd
import pyarrow.parquet as pq
import pyarrow as pa
import json

def get_oauth_token(client_id, client_secret, token_url):
    """
    Function to get OAuth token
    """
    data = {
        'grant_type': 'client_credentials',
        'client_id': client_id,
        'client_secret': client_secret
    }
    
    response = requests.post(token_url, data=data)
    response.raise_for_status()
    token = response.json().get('access_token')
    return token

def call_api(api_url, token, search_keys):
    """
    Function to call the API with a batch of search keys
    """
    headers = {
        'Authorization': f'Bearer {token}'
    }
    
    params = {
        'keys': ','.join(search_keys)  # Assuming the API accepts a comma-separated list of keys
    }
    
    response = requests.get(api_url, headers=headers, params=params)
    response.raise_for_status()
    return response.json()

def save_to_parquet(data, file_path):
    """
    Function to save JSON data to a Parquet file
    """
    df = pd.DataFrame(data)
    table = pa.Table.from_pandas(df)
    pq.write_table(table, file_path)

def save_to_json(data, file_path):
    """
    Function to save JSON data to a JSON file
    """
    with open(file_path, 'w') as json_file:
        json.dump(data, json_file, indent=4)

def main():
    client_id = 'your_client_id'
    client_secret = 'your_client_secret'
    token_url = 'https://example.com/oauth/token'
    api_url = 'https://example.com/api/search'
    csv_file_path = 'search_keys.csv'
    parquet_file_path = 'output.parquet'
    json_file_path = 'output.json'
    batch_size = 100  # Number of keys per request

    # Step 1: Get OAuth token
    token = get_oauth_token(client_id, client_secret, token_url)

    # Step 2: Read search keys from CSV file
    search_keys = pd.read_csv(csv_file_path)['key'].tolist()

    all_data = []

    # Step 3: Iterate through the search keys in batches and call the API
    for i in range(0, len(search_keys), batch_size):
        batch_keys = search_keys[i:i+batch_size]
        data = call_api(api_url, token, batch_keys)
        all_data.extend(data)

    # Step 4: Save the collected data to Parquet and JSON files
    save_to_parquet(all_data, parquet_file_path)
    save_to_json(all_data, json_file_path)
    print(f'Data has been saved to {parquet_file_path} and {json_file_path}')

if __name__ == '__main__':
    main()
