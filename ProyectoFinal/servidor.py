import socket
import logging
import random
import re


logging.basicConfig(level=logging.DEBUG,# nivel del mensaje que se quiere guardar o mostrar
                    format='%(asctime)s - %(threadName)s - %(processName)s - %(levelname)s - %(message)s',#formato del mensaje
                    filename='logger.log',#fichero donde se guarda los mensanjes
                    filemode='a')#actualizacion del fichero
def opciones(socket_server):
    opcion=socket_server.recv(1024).decode()
    opcion=opcion.toLowerCase()
    if opcion[0:3]=="ing":
        resultado = re.search(r'\d+', opcion).group()
        numero = int(resultado)
        ingresarDato(numero,socket_server)
    
        
ingresarDato(dinero,socket_server):
    
    
try:
    while True:
        socket_server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        socket_server.bind(("localhost", 2026))
        socket_server.listen(1)
        conn,addr=socket_server.accept()
        print("El servidor esperando....")
        logging.debug("El servidor esperando al número")
        print(opciones(conn))
except Exception as e:
    print(f"Error: {e}")
finally:
    socket_server.close()