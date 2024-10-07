import requests

# Set the server details
qrs_server = "https://your-qlik-sense-server"
session = requests.Session()

# Login URL (or any protected URL)
login_url = f"{qrs_server}/login"

# Get the login page
response = session.get(login_url)

if response.status_code == 200:
    # Print cookies to check if XSRF-TOKEN is present
    print("Cookies:", response.cookies)
    xsrf_token = response.cookies.get('XSRF-TOKEN')
    print("XSRF Token:", xsrf_token)
else:
    print(f"Failed to retrieve XSRF token. Status code: {response.status_code}")
