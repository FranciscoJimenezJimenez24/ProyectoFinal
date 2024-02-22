import socket
import ssl

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
    
def recibir_archivo(client_socket):
    # Recibir nombre del archivo
    nombre_archivo = client_socket.recv(1024).decode()
    nuevo_nombre = "movimientos.txt"    

    print(f"Recibiendo archivo: {nombre_archivo}")

    # Recibir y escribir el contenido del archivo
    with open(nuevo_nombre, "wb") as file:
        while True:
            data = client_socket.recv(1024)            
            if not data:
                break
            file.write(data)

    print(f"Archivo {nombre_archivo} recibido y guardado como {nuevo_nombre}.")
    
def control(socket_cliente):
    comando=""
    # si pone quit, se sal del menu 
    while comando!="quit":
        menu()
        comando=input("Comando: ").lower()
        socket_cliente.send(f"{comando}\r\n".encode())
        mensaje_servidor = socket_cliente.recv(1024).decode()
        print(mensaje_servidor)

        if comando.startswith("sendmov") :
            recibir_archivo(socket_cliente)



try:
    socket_cliente = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    
    socket_cliente.connect(("localhost", 2026)) 
    # mensaje_servidor = socket_cliente.recv(1024).decode()
    # print(mensaje_servidor)
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

