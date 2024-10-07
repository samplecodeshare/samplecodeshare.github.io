import requests

# Qlik Sense Server details
qrs_server = "https://your-qlik-sense-server"
task_name = "your-task-name"
certs = ('/path/to/client.pem', '/path/to/client_key.pem')
root_cert = '/path/to/root.pem'

# Headers for the request, including XSRF token and other necessary headers
headers = {
    'Content-Type': 'application/json',
    'X-Qlik-User': 'UserDirectory=YOUR_DOMAIN;UserId=YOUR_USER',
    'X-Qlik-XSRF-Token': 'your-xsrf-token'  # XSRF token to be included
}

# Function to get XSRF token (Example: You can extract it from a login request or browser cookies)
def get_xsrf_token():
    # Example login URL for getting the XSRF token
    login_url = f"{qrs_server}/login"
    response = requests.get(login_url, cert=certs, verify=root_cert)
    
    if response.status_code == 200:
        # Extract the XSRF token from response cookies or headers
        xsrf_token = response.cookies.get('XSRF-TOKEN')
        return xsrf_token
    else:
        print(f"Failed to get XSRF token. Status code: {response.status_code}")
        return None

# Function to find the Task ID by Name
def get_task_id_by_name(task_name, xsrf_token):
    qrs_api_url = f"{qrs_server}/qrs/task/full"
    
    headers['X-Qlik-XSRF-Token'] = xsrf_token  # Include XSRF token in the header
    
    response = requests.get(qrs_api_url, headers=headers, cert=certs, verify=root_cert)
    
    if response.status_code == 200:
        tasks = response.json()
        for task in tasks:
            if task['name'] == task_name:
                return task['id']
    return None

# Example usage: Get the XSRF token and start the task
xsrf_token = get_xsrf_token()
if xsrf_token:
    task_id = get_task_id_by_name(task_name, xsrf_token)
    print(f"Task ID: {task_id}")
else:
    print("Failed to retrieve XSRF token.")
