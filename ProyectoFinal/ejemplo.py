import socket
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.backends import default_backend

def main():
    host = '127.0.0.1'
    port = 2026

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.connect((host, port))
        print("Connected to server.")

        with open('received_file.txt', 'wb') as f:
            print("Receiving encrypted file from server...")
            while True:
                data = s.recv(1024)
                if not data:
                    break
                f.write(data)

        print("Encrypted file received from server.")

        # Decrypt the received file
        decrypt_file('received_file.txt')

def decrypt_file(filename):
    aes_key = b'0123456789abcdef'  # 16-byte key for AES decryption
    cipher = Cipher(algorithms.AES(aes_key), modes.ECB(), backend=default_backend())
    decryptor = cipher.decryptor()
    with open(filename, 'rb') as f:
        encrypted_data = f.read()
    decrypted_data = decryptor.update(encrypted_data) + decryptor.finalize()
    with open(filename, 'wb') as f:
        f.write(decrypted_data)
    print("File decrypted.")

if __name__ == "__main__":
    main()