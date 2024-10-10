import ssl
import asyncio
import websockets

# Paths to your client certificate, private key, and root CA
client_cert = "/path/to/client.pem"
client_key = "/path/to/client_key.pem"
root_ca = "/path/to/root.pem"

# Qlik Sense Engine WebSocket URL
wss_url = "wss://<qlik-sense-server>:4747/app/<app-id>"

# SSL context setup
ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLS_CLIENT)
ssl_context.load_cert_chain(certfile=client_cert, keyfile=client_key)
ssl_context.load_verify_locations(cafile=root_ca)  # Optional if required

async def test_wss_connection():
    try:
        # Attempt to connect to the WebSocket server
        async with websockets.connect(wss_url, ssl=ssl_context) as websocket:
            print("Connection established successfully!")
            # Optionally, you can send a basic request
            request_payload = {
                "jsonrpc": "2.0",
                "id": 1,
                "method": "GetDocList",
                "handle": -1,
                "params": []
            }
            await websocket.send(json.dumps(request_payload))

            # Receive the response
            response = await websocket.recv()
            print("Response from server:", response)
    except Exception as e:
        print(f"Failed to connect: {e}")

# Run the asynchronous test
asyncio.get_event_loop().run_until_complete(test_wss_connection())
