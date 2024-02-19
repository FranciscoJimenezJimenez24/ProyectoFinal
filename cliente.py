import socket
import ssl

def menu():
    print("""
        (ING <money>) Ingresa dinero
        (RET <money>) Retirar dinero
        (MOV) Recuperar mensaje
        (QUIT) Abandonar la sesión de cliente
        """)
# este metodo pide el usuario
def user(socket_cliente):

    usuario=input("Introduce el usuario: ")
    usuario= "user "+usuario
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
    contrasena= "pass "+contrasena
    socket_cliente.send(f"{contrasena}\r\n".encode())
    mensaje_servidor = socket_cliente.recv(1024).decode()
    print(mensaje_servidor)
    if (mensaje_servidor[0:3]=="+OK"):
        return True
    else:
        return False
    
def control(socket_cliente):
    comando=""
    # si pone quit, se sal del menu
    while comando!="quit":
        menu()
        comando=input("Comando: ").lower()
        socket_cliente.send(f"{comando}\r\n".encode())
        mensaje_servidor = socket_cliente.recv(1024).decode()
        print(mensaje_servidor)

try:
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    # Crear un contexto SSL/TLS
    context = ssl.create_default_context()
    context.check_hostname = False  # Desactivar check_hostname
    context.verify_mode = ssl.CERT_NONE
    
    socket_cliente = context.wrap_socket(sock, server_hostname="localhost")
    socket_cliente.connect(("localhost", 2026))

    mensaje_servidor = socket_cliente.recv(1024).decode()
    print(mensaje_servidor)
    num = 1
    while (num!=0):
        if (user(socket_cliente)):
            # si la contraseña es incorrecta, vuelve ha preguntar el usuario
            if (password(socket_cliente)):
                num=0
                control(socket_cliente)
except Exception as e:
    print(f"Error: {e}")
finally:
    socket_cliente.close()

