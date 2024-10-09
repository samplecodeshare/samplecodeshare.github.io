# pip install fastapi uvicorn PyJWT

from fastapi import FastAPI, Request, Depends, HTTPException
from fastapi.security import OAuth2PasswordBearer
import jwt
from jwt import PyJWTError

# A sample secret key (in practice, store securely!)
SECRET_KEY = 'your_secret_key'
ALGORITHM = 'HS256'  # Algorithm used for encoding/decoding the JWT token

app = FastAPI()

# OAuth2PasswordBearer instance (Bearer token extraction)
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")


# A function to simulate token generation (for testing purposes)
def generate_token(user_id, password):
    # Create a JWT token
    token = jwt.encode({'user_id': user_id, 'password': password}, SECRET_KEY, algorithm=ALGORITHM)
    return token


# Dependency to decode and validate JWT token
def decode_token(token: str):
    try:
        # Decode the JWT token
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        user_id: str = payload.get("user_id")
        password: str = payload.get("password")
        if user_id is None or password is None:
            raise HTTPException(status_code=401, detail="Invalid token")
        return {"user_id": user_id, "password": password}
    except PyJWTError:
        raise HTTPException(status_code=401, detail="Invalid token")


# API Endpoint to handle the request with Bearer token
@app.get("/api/endpoint")
async def api_endpoint(token: str = Depends(oauth2_scheme)):
    # Decode the token and extract user_id and password
    user_info = decode_token(token)
    
    return {
        "message": "Token decoded successfully",
        "user_id": user_info['user_id'],
        "password": user_info['password']  # Not recommended to return raw password; for demo only
    }


# Endpoint to generate a test token (you can remove this in production)
@app.get("/generate-token")
def get_token():
    # Example values
    token = generate_token("example_user", "example_password")
    return {"token": token}


if __name__ == "__main__":
    # To run the server, use: uvicorn main:app --reload
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000, reload=True)
