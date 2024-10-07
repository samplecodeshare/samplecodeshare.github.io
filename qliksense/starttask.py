import requests
import json

# Qlik Sense QRS API Server Details
qrs_server = "https://your-qlik-sense-server"
qrs_port = "4242"
task_name = "your-task-name"

# Certificates for authenticating the API call (client and key)
certs = ('/path/to/client.pem', '/path/to/client_key.pem')
root_cert = '/path/to/root.pem'

# Headers for the request, including Qlik user details
headers = {
    'Content-Type': 'application/json',
    'X-Qlik-User': 'UserDirectory=YOUR_DOMAIN;UserId=YOUR_USER'
}

# Function to find the Task ID by Task Name
def get_task_id_by_name(task_name):
    qrs_api_url = f"{qrs_server}:{qrs_port}/qrs/task/full"
    
    # Send GET request to retrieve all tasks
    response = requests.get(qrs_api_url, headers=headers, cert=certs, verify=root_cert)
    
    if response.status_code == 200:
        tasks = response.json()
        
        # Loop through tasks to find the task by name
        for task in tasks:
            if task['name'] == task_name:
                return task['id']  # Return the task ID if the name matches
    
    return None  # Return None if the task is not found

# Function to start a task with parameters
def start_task_with_params(task_id, input_params):
    # QRS API URL for starting the task synchronously
    qrs_api_url = f"{qrs_server}:{qrs_port}/qrs/task/{task_id}/start/synchronous"
    
    # Prepare the payload with custom parameters
    payload = {
        "params": input_params  # Custom parameters passed in the payload
    }

    # Send POST request to start the task
    response = requests.post(qrs_api_url, json=payload, headers=headers, cert=certs, verify=root_cert)

    # Check the response
    if response.status_code == 200:
        print(f"Task {task_id} started successfully.")
    else:
        print(f"Failed to start task {task_id}. Status code: {response.status_code}, Response: {response.text}")

# Main code execution
if __name__ == "__main__":
    # Step 1: Get Task ID by Name
    task_id = get_task_id_by_name(task_name)
    
    if task_id:
        print(f"Found Task ID: {task_id}")
        
        # Step 2: Define input parameters to pass to the task
        input_parameters = {
            "CustomParameter1": "Value1",
            "CustomParameter2": "Value2"
        }
        
        # Step 3: Start the task and pass parameters
        start_task_with_params(task_id, input_parameters)
    else:
        print(f"Task with name '{task_name}' not found.")
