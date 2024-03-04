import socket
import ssl
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.primitives import padding
from cryptography.hazmat.backends import default_backend

usuario1=""
contrasena1=""


# este metodo pide el usuario
def user(socket_cliente):
    global usuario1
    usuario=input("Introduce el usuario: ")
    usuario1=usuario
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
    global contrasena1
    contrasena=input("Introduce la contraseña: ")
    contrasena1=contrasena
    socket_cliente.send(f"{contrasena}\r\n".encode())
    mensaje_servidor = socket_cliente.recv(1024).decode()
    print(mensaje_servidor)
    if (mensaje_servidor[0:3]=="+OK"):
        return True
    else:
        return False
    
def menu():
    if usuario1=="usuario1" and contrasena1=="contrasena1":
        print("""
            (ING <money> <id_cuenta>) Ingresa dinero
            (RET <money> <id_cuenta>) Retirar dinero
            (MOV <id_cuenta>) ver ultimos movimientos
            (SENDMOV <id_cuenta>) Enviar fichero con movimiento al cliente
            (SEECOUNTS) ver el saldo de todas las cuentas
            (QUIT) Abandonar la sesión de cliente
            """)
    else:
        print("""
            (ING <money> <id_cuenta>) Ingresa dinero
            (RET <money> <id_cuenta>) Retirar dinero
            (MOV <id_cuenta>) ver ultimos movimientos
            (SENDMOV <id_cuenta>) Enviar fichero con movimiento al cliente
            (QUIT) Abandonar la sesión de cliente
            """)
    
def decrypt_data(encrypted_data, iv):
    aes_key = b'0123456789abcdef'  # 16-byte key for AES decryption
    cipher = Cipher(algorithms.AES(aes_key), modes.CBC(iv), backend=default_backend())
    decryptor = cipher.decryptor()
    decrypted_data = decryptor.update(encrypted_data) + decryptor.finalize()
    print("Data decrypted.")
    unpadder = padding.PKCS7(128).unpadder()
    decrypted_data = unpadder.update(decrypted_data) + unpadder.finalize()
    return decrypted_data

def control(socket_cliente):
    comando=""
    # si pone quit, se sale del menu 
    while comando!="quit":
        menu()
        comando=input("Comando: ").lower()
        socket_cliente.send(f"{comando}\r\n".encode())
        if comando.startswith("sendmov") :
            encrypted_data = socket_cliente.recv(1024)
            iv = socket_cliente.recv(16)  # Recibir el vector de inicialización
            decrypted_data = decrypt_data(encrypted_data, iv)
            with open('movimientos.txt', 'wb') as f:
                f.write(decrypted_data)
            print("File decrypted and saved as 'movimientos.txt'.")

        mensaje_servidor = socket_cliente.recv(1024).decode()
        print(mensaje_servidor)

try:
    context = ssl.create_default_context()
    context.check_hostname = False  # Desactivar la verificación del hostname
    context.verify_mode = ssl.CERT_NONE  # Desactivar la verificación del certificado
    with context.wrap_socket(socket.socket(socket.AF_INET, socket.SOCK_STREAM), server_hostname="localhost") as socket_cliente:
        socket_cliente.connect(("localhost", 2026))
        num = 1
        while num != 0:
            if user(socket_cliente):
                if password(socket_cliente):
                    num = 0
                    control(socket_cliente)
except Exception as e:
    print(f"Error: {e}")
finally:
    try:
        socket_cliente.close()
    except NameError:
        pass

