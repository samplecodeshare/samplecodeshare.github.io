from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.security import HTTPBasic, HTTPBasicCredentials
import secrets

app = FastAPI()

# Dependency for Basic Authentication
security = HTTPBasic()


# API Endpoint that requires Basic Authentication
@app.get("/basic-auth-endpoint")
def basic_auth(credentials: HTTPBasicCredentials = Depends(security)):
    # Extract the username and password
    user_id = credentials.username
    password = credentials.password

    # In a real scenario, you would validate the user ID and password here
    # For demonstration, we are using a simple static check
    correct_username = secrets.compare_digest(user_id, "example_user")
    correct_password = secrets.compare_digest(password, "example_password")

    if not (correct_username and correct_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid credentials",
            headers={"WWW-Authenticate": "Basic"},
        )

    return {
        "message": "Basic Authentication successful",
        "user_id": user_id,
        "password": password  # Do not expose password in a real scenario, this is just for demonstration.
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000, reload=True)
