import requests
import json

# Replace with your Qlik Sense QMC URL and authentication details
qmc_url = "https://your_qmc_url/qrs"
username = "your_username"
password = "your_password"

# Specify the task ID and script parameters
task_id = "your_task_id"
script_parameters = {
    "param1": "value1",
    "param2": "value2",
    # ... other parameters
}

# Prepare the request headers with authentication
headers = {
    "Content-Type": "application/json",
    "Authorization": f"Basic {base64.b64encode(f'{username}:{password}'.encode('utf-8')).decode('utf-8')}"
}

# Construct the request body with task ID and parameters
data = {
    "taskId": task_id,
    "scriptParameters": script_parameters
}

# Make the POST request to start the task
response = requests.post(f"{qmc_url}/tasks", headers=headers, data=json.dumps(data))

# Check the response status
if response.status_code == 200:
    print("Task started successfully.")
else:
    print("Error starting task:", response.text)
