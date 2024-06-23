import pandas as pd
import json
import requests

# Step 1: Read the Excel file
file_path = 'data.xlsx'
df = pd.read_excel(file_path)

# Step 2: Convert the DataFrame to JSON
data_json = df.to_json(orient='records')
data_list = json.loads(data_json)

# Step 3: Send the JSON data to the REST API
api_url = 'https://your-api-endpoint.com/your-api-path'

headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
}

for record in data_list:
    response = requests.post(api_url, headers=headers, json=record)
    if response.status_code == 200:
        print(f"Successfully sent data for id {record['id']}")
    else:
        print(f"Failed to send data for id {record['id']}, status code: {response.status_code}, response: {response.text}")
