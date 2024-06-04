import pandas as pd
import json

def csv_to_json_template_in_batches(csv_file_path, template, chunksize=2):
    # Initialize the list to hold the JSON data
    json_data = []

    # Read the CSV file in chunks
    for chunk in pd.read_csv(csv_file_path, chunksize=chunksize):
        # Process each chunk
        for _, row in chunk.iterrows():
            item = template.copy()  # Use a copy of the template for each row
            item['key'] = row['key']
            item['value'] = row['value']
            json_data.append(item)
    
    # Convert the list to a JSON string
    json_string = json.dumps(json_data, indent=4)
    return json_string

# Define the JSON template
template = {
    "key": "",
    "value": ""
}

csv_file_path = 'data.csv'
json_string = csv_to_json_template_in_batches(csv_file_path, template, chunksize=2)

print(json_string)
