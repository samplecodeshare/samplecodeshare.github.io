import ssl
import json
import asyncio
import websockets

# Paths to your client certificate, private key, and root CA
client_cert = "/path/to/client.pem"
client_key = "/path/to/client_key.pem"
root_ca = "/path/to/root.pem"

# Qlik Sense Engine WebSocket URL
wss_url = "wss://<qlik-sense-server>:4747/app/"

# SSL context setup
ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLS_CLIENT)
ssl_context.load_cert_chain(certfile=client_cert, keyfile=client_key)
ssl_context.load_verify_locations(cafile=root_ca)  # Optional if needed

# Function to list all apps using the QIX API
async def list_apps():
    try:
        # Connect to the Qlik Sense WebSocket server
        async with websockets.connect(wss_url, ssl=ssl_context) as websocket:
            # Create the GetDocList request payload
            request_payload = {
                "jsonrpc": "2.0",
                "id": 1,
                "method": "GetDocList",
                "handle": -1,
                "params": []
            }

            # Send the request to list all apps
            await websocket.send(json.dumps(request_payload))

            # Receive the response from the server
            response = await websocket.recv()
            response_data = json.loads(response)

            # Parse and print the list of apps
            if "result" in response_data:
                apps = response_data["result"]["qDocList"]
                for app in apps:
                    print(f"App Name: {app['qTitle']}, App ID: {app['qDocId']}")
            else:
                print("Failed to retrieve app list:", response_data)

    except Exception as e:
        print(f"Error during connection: {e}")

# Run the asynchronous function
asyncio.get_event_loop().run_until_complete(list_apps())
