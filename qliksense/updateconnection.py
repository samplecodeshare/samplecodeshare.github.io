import requests
import json

# Path to your Qlik Sense certificates (exported from QMC)
certificates = (
    'path_to_client.pem',  # Client certificate
    'path_to_client_key.pem'  # Client key
)

# Base URL of Qlik Sense QRS API
qrs_base_url = 'https://<qlik-sense-server>:4242/qrs'

# Disable SSL warnings (not recommended for production)
requests.packages.urllib3.disable_warnings()

# Function to update the REST connection's authentication details
def update_rest_connection(connection_id, new_username, new_password):
    # QRS API URL for the specific REST connection
    url = f"{qrs_base_url}/dataconnection/{connection_id}"
    
    # Fetch the existing connection details
    response = requests.get(url, cert=certificates, verify=False)
    
    if response.status_code != 200:
        raise Exception(f"Failed to retrieve the connection. Status code: {response.status_code}")
    
    # Load the connection data
    connection_data = response.json()

    # Update the username and password for Basic Authentication
    connection_data['username'] = new_username
    connection_data['password'] = new_password
    
    # Send the updated data back to QRS API
    headers = {
        'X-Qlik-User': 'UserDirectory=Internal;UserId=sa_repository'  # Replace with correct admin user if needed
    }

    update_response = requests.put(url, headers=headers, data=json.dumps(connection_data), cert=certificates, verify=False)

    if update_response.status_code == 200:
        print("Successfully updated the connection's Basic Authentication details.")
    else:
        print(f"Failed to update connection. Status code: {update_response.status_code}, Response: {update_response.text}")

# Example Usage
if __name__ == "__main__":
    connection_id = '<your_rest_connection_id>'  # Replace with the actual REST connection ID
    new_username = 'new_user'
    new_password = 'new_password'

    update_rest_connection(connection_id, new_username, new_password)
