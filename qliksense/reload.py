import requests
import json

# Define paths to your Qlik Sense certificates and key
cert_path = ('/path/to/client.pem', '/path/to/client_key.pem')

# Qlik Sense server URL (replace with your server address)
server_url = 'https://your-qliksense-server.com'

# Qlik Sense QRS API endpoint to start a task (replace {task_id} with your task ID)
task_id = 'your-task-id'  # Replace with the actual task ID
qrs_api_url_task = f'{server_url}/qrs/task/{task_id}/start/synchronous'

# Headers for the request
headers = {
    'Content-Type': 'application/json',
    'X-Qlik-User': 'UserDirectory=INTERNAL;UserId=sa_repository'  # Change as per your setup
}

# New parameters to be passed to the reload script
new_script_parameters = [
    {
        "name": "Parameter1",
        "value": "Value1"
    },
    {
        "name": "Parameter2",
        "value": "Value2"
    }
]

# QRS API URL to update the reload task
qrs_api_url_reload = f'{server_url}/qrs/reloadtask/{task_id}'

# Step 1: Get the existing task configuration (to modify it)
try:
    response_get = requests.get(qrs_api_url_reload, headers=headers, cert=cert_path, verify=False)

    if response_get.status_code == 200:
        task_config = response_get.json()

        # Step 2: Add the new script parameters to the task configuration
        task_config['scriptParameters'] = new_script_parameters

        # Step 3: Update the task with the new script parameters
        response_update = requests.put(qrs_api_url_reload, headers=headers, cert=cert_path, data=json.dumps(task_config), verify=False)

        if response_update.status_code == 200:
            print("Task parameters updated successfully.")
        else:
            print(f'Failed to update task parameters. Status code: {response_update.status_code}')
            print(response_update.text)
    else:
        print(f'Failed to retrieve task configuration. Status code: {response_get.status_code}')
        print(response_get.text)

except requests.exceptions.RequestException as e:
    print(f'An error occurred: {e}')

# Step 4: Start the reload task
try:
    response_start = requests.post(qrs_api_url_task, headers=headers, cert=cert_path, verify=False)

    if response_start.status_code == 200:
        print('Task started successfully:', response_start.json())
    else:
        print(f'Failed to start task. Status code: {response_start.status_code}')
        print(response_start.text)

except requests.exceptions.RequestException as e:
    print(f'An error occurred: {e}')
