import re

cadena = "ING <300>"
resultado = re.search(r'\d+', cadena).group()
numero = int(resultado)
print(numero)