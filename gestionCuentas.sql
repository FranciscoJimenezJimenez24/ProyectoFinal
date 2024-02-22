DROP DATABASE IF EXISTS gestionCuentas;
CREATE DATABASE IF NOT EXISTS gestionCuentas;
USE gestionCuentas;

CREATE TABLE IF NOT EXISTS usuarios(
	id_usuario INT PRIMARY KEY AUTO_INCREMENT,
    usuario VARCHAR(60) UNIQUE,
    contrasena VARCHAR(60)
);

CREATE TABLE IF NOT EXISTS cuentas(
	id_cuenta INT PRIMARY KEY AUTO_INCREMENT,
    saldo DECIMAL(10,2),
    id_usuario INT,
    num_movimientos INT,
    FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario)
);

CREATE TABLE IF NOT EXISTS movimientos(
	id_movimiento INT PRIMARY KEY AUTO_INCREMENT,
    dinero_movido DECIMAL(10,2),
    fecha DATETIME,
    id_cuenta INT,
	FOREIGN KEY (id_cuenta) REFERENCES cuentas (id_cuenta)
);

INSERT INTO usuarios (usuario, contrasena) VALUES
('usuario1', 'contrasena1'),
('usuario2', 'contrasena2'),
('usuario3', 'contrasena3');

-- Insertar algunas cuentas vinculadas a los usuarios
INSERT INTO cuentas (saldo, id_usuario, num_movimientos) VALUES
(1000.00, 1, 0),  -- La cuenta 1 está vinculada al usuario con id_usuario = 1
(500.00, 1, 0),   -- La cuenta 2 también está vinculada al usuario con id_usuario = 1
(2000.00, 2, 0);  -- La cuenta 3 está vinculada al usuario con id_usuario = 2
