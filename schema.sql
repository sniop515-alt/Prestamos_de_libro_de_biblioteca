-- Sistema de biblioteca: 6 tablas relacionadas (MySQL)
CREATE DATABASE IF NOT EXISTS biblioteca CHARACTER SET utf8mb4;
USE biblioteca;

CREATE TABLE socio (
    id_socio     INT AUTO_INCREMENT PRIMARY KEY,
    cedula       VARCHAR(13) NOT NULL UNIQUE,
    nombres      VARCHAR(60) NOT NULL,
    apellidos    VARCHAR(60) NOT NULL,
    telefono     VARCHAR(15),
    email        VARCHAR(80)
) ENGINE=InnoDB;

CREATE TABLE bibliotecario (
    id_bibliotecario INT AUTO_INCREMENT PRIMARY KEY,
    nombres          VARCHAR(60) NOT NULL,
    apellidos        VARCHAR(60) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE autor (
    id_autor INT AUTO_INCREMENT PRIMARY KEY,
    nombre   VARCHAR(80) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE libro (
    id_libro     INT AUTO_INCREMENT PRIMARY KEY,
    isbn         VARCHAR(20) NOT NULL UNIQUE,
    titulo       VARCHAR(120) NOT NULL,
    id_autor     INT NOT NULL,
    disponibles  INT NOT NULL DEFAULT 0,
    FOREIGN KEY (id_autor) REFERENCES autor(id_autor)
) ENGINE=InnoDB;

CREATE TABLE prestamo (
    id_prestamo      INT AUTO_INCREMENT PRIMARY KEY,
    fecha_prestamo   DATE NOT NULL,
    fecha_prevista   DATE NOT NULL,
    estado           VARCHAR(10) NOT NULL DEFAULT 'Activo',
    id_socio         INT NOT NULL,
    id_bibliotecario INT NOT NULL,
    FOREIGN KEY (id_socio)         REFERENCES socio(id_socio),
    FOREIGN KEY (id_bibliotecario) REFERENCES bibliotecario(id_bibliotecario)
) ENGINE=InnoDB;

CREATE TABLE detalle_prestamo (
    id_detalle       INT AUTO_INCREMENT PRIMARY KEY,
    id_prestamo      INT NOT NULL,
    id_libro         INT NOT NULL,
    devuelto         TINYINT(1) NOT NULL DEFAULT 0,
    fecha_devolucion DATE NULL,
    FOREIGN KEY (id_prestamo) REFERENCES prestamo(id_prestamo) ON DELETE CASCADE,
    FOREIGN KEY (id_libro)    REFERENCES libro(id_libro)
) ENGINE=InnoDB;

-- Datos de ejemplo
INSERT INTO socio (cedula, nombres, apellidos, telefono, email) VALUES
 ('1300000001', 'Ana',  'Mero',     '0991111111', 'ana@correo.com'),
 ('1300000002', 'Luis', 'Zambrano', '0992222222', 'luis@correo.com');

INSERT INTO bibliotecario (nombres, apellidos) VALUES
 ('Carlos', 'Vera'),
 ('María',  'Loor');

INSERT INTO autor (nombre) VALUES
 ('Gabriel García Márquez'), ('Jorge Icaza'), ('Miguel de Cervantes');

INSERT INTO libro (isbn, titulo, id_autor, disponibles) VALUES
 ('978-0307474728', 'Cien años de soledad',        1, 3),
 ('978-9978180013', 'Huasipungo',                  2, 2),
 ('978-8420412146', 'Don Quijote de la Mancha',    3, 2),
 ('978-0307387264', 'El amor en los tiempos del cólera', 1, 1);
