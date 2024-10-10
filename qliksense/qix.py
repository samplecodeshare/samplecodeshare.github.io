import ssl
import json
import asyncio
import websockets

# Paths to your client certificate, private key, and root CA
client_cert = "/path/to/client.pem"
client_key = "/path/to/client_key.pem"
root_ca = "/path/to/root.pem"  # Optional if needed

# Qlik Sense Engine WebSocket URL
qlik_engine_url = "wss://<qlik-sense-server>:4747/app/<app-id>"

# SSL context setup
ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLS_CLIENT)
ssl_context.load_cert_chain(certfile=client_cert, keyfile=client_key)
ssl_context.load_verify_locations(cafile=root_ca)  # Optional: verify server cert


# Example function to update app variables via QIX API
async def update_variables(websocket, variable_name, value):
    request_payload = {
        "jsonrpc": "2.0",
        "id": 1,
        "method": "SetVariable",
        "handle": 1,
        "params": [variable_name, value]
    }
    await websocket.send(json.dumps(request_payload))
    response = await websocket.recv()
    return json.loads(response)

async def set_variables_and_reload():
    # Open WebSocket connection to Qlik Sense Engine
    async with websockets.connect(qlik_engine_url, ssl=ssl_context) as websocket:
        
        # Example: Set parameters via variables
        await update_variables(websocket, "myParam1", 42)
        await update_variables(websocket, "myParam2", "custom-value")
        
        # Trigger reload of the app
        reload_request = {
            "jsonrpc": "2.0",
            "id": 2,
            "method": "DoReload",
            "handle": 1,
            "params": [0, False, False]  # 0: Full reload, False: Partial reload, False: Debug
        }
        await websocket.send(json.dumps(reload_request))

        # Get the response after reload starts
        reload_response = await websocket.recv()
        print("Reload Response:", reload_response)

        # Optionally save the app after reloading
        save_request = {
            "jsonrpc": "2.0",
            "id": 3,
            "method": "DoSave",
            "handle": 1,
            "params": []
        }
        await websocket.send(json.dumps(save_request))
        save_response = await websocket.recv()
        print("Save Response:", save_response)

# Run the asynchronous task
asyncio.get_event_loop().run_until_complete(set_variables_and_reload())
