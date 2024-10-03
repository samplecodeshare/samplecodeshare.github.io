# pip install cryptography

from cryptography.fernet import Fernet

# Generate a key for encryption
key = Fernet.generate_key()

# Save the key to a file (store this securely)
with open('secret.key', 'wb') as key_file:
    key_file.write(key)

print(f"Key generated and saved to 'secret.key'")



#Decript/Encript

from cryptography.fernet import Fernet

# Load the previously generated key
with open('secret.key', 'rb') as key_file:
    key = key_file.read()

# Create a Fernet instance
cipher_suite = Fernet(key)

# The password you want to encrypt
password = "your_password"

# Encrypt the password
encrypted_password = cipher_suite.encrypt(password.encode())

print(f"Encrypted password: {encrypted_password.decode()}")
