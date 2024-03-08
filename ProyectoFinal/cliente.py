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
            (ING <money> <id_cuenta>) Ingresa dinero/ejemplo: ing <300> 1
            (RET <money> <id_cuenta>) Retirar dinero/ejemplo: ret <300> 1
            (MOV <id_cuenta>) ver últimos 10 movimientos/ejemplo: mov 1
            (SENDMOV <id_cuenta>) Enviar fichero con movimiento al cliente/ejemplo: sendmov 1
            (SEECOUNTS) ver el saldo de todas las cuentas
            (QUIT) Abandonar la sesión de cliente
            """)
    else:
        print("""
            (ING <money> <id_cuenta>) Ingresa dinero/ejemplo: ing <300> 1
            (RET <money> <id_cuenta>) Retirar dinero/ejemplo: ret <300> 1
            (MOV <id_cuenta>) ver últimos 10 movimientos/ejemplo: mov 1
            (SENDMOV <id_cuenta>) Enviar fichero con movimiento al cliente/ejemplo: sendmov 1
            (QUIT) Abandonar la sesión de cliente
            """)

def control(socket_cliente):
    comando=""
    # si pone quit, se sale del menu 
    while comando!="quit":
        menu()
        comando=input("Comando: ").lower()
        socket_cliente.send(f"{comando}\r\n".encode())
        if comando.startswith("sendmov"):
            encrypted_data = socket_cliente.recv(1024)
            if encrypted_data != b'Esa cuenta no existe\r\n' and encrypted_data != b"Ese comando es incorrecto\r\n":
                with open('movimientos.txt', 'wb') as f:
                    while True:
                        data = socket_cliente.recv(1024)            
                        if not data:
                            break
                        f.write(data)
                print("File received successfully.")
                mensaje_servidor = socket_cliente.recv(1024).decode()
                print(mensaje_servidor)
            else:
                # Se recibió un mensaje de error
                mensaje = encrypted_data.decode()
                print("Error:", mensaje.strip())
        else:
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

