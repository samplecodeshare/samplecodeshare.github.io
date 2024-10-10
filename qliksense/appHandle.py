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
