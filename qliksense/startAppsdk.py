from qlik_sdk import CloudClient
import ssl

# Set up the connection to the Qlik Sense Enterprise server using certificate-based authentication
client = CloudClient(host="https://<qlik-sense-server>", cert_file="/path/to/client.pem", key_file="/path/to/client_key.pem", ca_file="/path/to/root.pem")

# Function to open an app by its App ID
def open_app(app_id):
    try:
        # Open the app
        app = client.open_app(app_id)
        
        # Print basic app information
        print(f"App Name: {app.get('qTitle')}")
        print(f"App ID: {app_id}")

        # Optionally, you can reload the app's data or perform other operations here
        # Reload the app
        app.do_reload()

        print(f"App {app_id} reloaded successfully.")
    except Exception as e:
        print(f"Failed to open the app: {e}")

# Replace with your app's App ID
app_id = "3fbb6d84-4b7e-45ab-9148-8fc8d31c923c"
open_app(app_id)
