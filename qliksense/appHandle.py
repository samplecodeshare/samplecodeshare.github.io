async def open_app(websocket, app_id):
    # Open the app and retrieve the handle
    request_payload = {
        "jsonrpc": "2.0",
        "id": 1,
        "method": "OpenDoc",
        "handle": -1,
        "params": [app_id]
    }
    await websocket.send(json.dumps(request_payload))
    response = await websocket.recv()
    response_data = json.loads(response)
    
    # Get the app handle (usually a number like 1 or 2)
    app_handle = response_data.get('result', {}).get('qReturn', {}).get('qHandle', None)
    
    if app_handle is None:
        raise Exception("Failed to open app. Handle not found.")
    
    return app_handle

async def set_variables_and_reload(app_id):
    # Open WebSocket connection to Qlik Sense Engine
    async with websockets.connect(qlik_engine_url, ssl=ssl_context) as websocket:
        
        # Open the app and get the handle
        app_handle = await open_app(websocket, app_id)
        
        # Example: Set variables via QIX API
        await update_variables(websocket, app_handle, "myParam1", 42)
        await update_variables(websocket, app_handle, "myParam2", "custom-value")
        
        # Trigger reload of the app
        reload_request = {
            "jsonrpc": "2.0",
            "id": 2,
            "method": "DoReload",
            "handle": app_handle,  # Use the valid app handle here
            "params": [0, False, False]  # Full reload, no partial reload, no debug
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
            "handle": app_handle  # Use the valid app handle here
        }
        await websocket.send(json.dumps(save_request))
        save_response = await websocket.recv()
        print("Save Response:", save_response)

# Run the asynchronous task
asyncio.get_event_loop().run_until_complete(set_variables_and_reload("<your-app-id>"))
