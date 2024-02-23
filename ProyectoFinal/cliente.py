import socket
import ssl
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.backends import default_backend

def menu():
    print("""
        (ING <money> <id_cuenta>) Ingresa dinero
        (RET <money> <id_cuenta>) Retirar dinero
        (MOV <id_cuenta>) Recuperar mensaje
        (SENDMOV <id_cuenta>) Enviar fichero con movimiento al cliente
        (QUIT) Abandonar la sesión de cliente
        """)
# este metodo pide el usuario
def user(socket_cliente):

    usuario=input("Introduce el usuario: ")
    # envia el usuario
    socket_cliente.send(f"{usuario}\r\n".encode())
    # recibe una respuesta
    mensaje_servidor = socket_cliente.recv(1024).decode()
    print(mensaje_servidor)
    # si tiene el mensaje un +OK, significa que todo se ha hecho bien
    if (mensaje_servidor[0:3]=="+OK"):
        return True
    else:
        return False

# este metodo pide la contraseña
def password(socket_cliente):
    contrasena=input("Introduce la contraseña: ")
    socket_cliente.send(f"{contrasena}\r\n".encode())
    mensaje_servidor = socket_cliente.recv(1024).decode()
    print(mensaje_servidor)
    if (mensaje_servidor[0:3]=="+OK"):
        return True
    else:
        return False
    
def decrypt_data(encrypted_data):
    aes_key = b'0123456789abcdef'  # 16-byte key for AES decryption
    cipher = Cipher(algorithms.AES(aes_key), modes.ECB(), backend=default_backend())
    decryptor = cipher.decryptor()
    decrypted_data = decryptor.update(encrypted_data) + decryptor.finalize()
    print("Data decrypted.")
    return decrypted_data
    
def control(socket_cliente):
    comando=""
    # si pone quit, se sal del menu 
    while comando!="quit":
        menu()
        comando=input("Comando: ").lower()
        socket_cliente.send(f"{comando}\r\n".encode())
        if comando.startswith("sendmov") :
            encrypted_data = socket_cliente.recv(1024)
            decrypted_data = decrypt_data(encrypted_data)
            with open('movimientos.txt', 'wb') as f:
                f.write(decrypted_data)
            print("File decrypted and saved as 'movimientos.txt'.")

        mensaje_servidor = socket_cliente.recv(1024).decode()
        print(mensaje_servidor)

        



try:
    socket_cliente = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    
    socket_cliente.connect(("localhost", 2026)) 
    # mensaje_servidor = socket_cliente.recv(1024).decode()
    # print(mensaje_servidor)
    num = 1
    while (num!=0):
        if (user(socket_cliente)):
            if (password(socket_cliente)):
                num=0
                control(socket_cliente)
except Exception as e:
    print(f"Error: {e}")
finally:
    socket_cliente.close()

