-- BORRADO DE TABLAS (Orden inverso para no romper FKs) --
DROP TABLE IF EXISTS opinion;
DROP TABLE IF EXISTS reserva;
DROP TABLE IF EXISTS casa_rural;
DROP TABLE IF EXISTS huesped;
DROP TABLE IF EXISTS propietario;

-- 1. TABLA PROPIETARIO --
CREATE TABLE propietario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    telefono VARCHAR(12) NOT NULL UNIQUE
);

-- 2. TABLA HUESPED --
CREATE TABLE huesped (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    dni VARCHAR(20) NOT NULL UNIQUE,
    telefono VARCHAR(12) NOT NULL UNIQUE
);

-- 3. TABLA CASA RURAL (Hija de Propietario) --
CREATE TABLE casa_rural (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    precio_noche DOUBLE NOT NULL,
    capacidad_personas BIGINT,
    propietario_id BIGINT NOT NULL,
    CONSTRAINT fk_casa_propietario FOREIGN KEY (propietario_id) REFERENCES propietario(id)
);

-- 4. TABLA RESERVA (Hija de Casa y Huesped) --
CREATE TABLE reserva (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha_entrada DATE NOT NULL,
    fecha_salida DATE NOT NULL,
    importe_total DOUBLE,
    casa_rural_id BIGINT NOT NULL,
    huesped_id BIGINT NOT NULL,
    CONSTRAINT fk_reserva_casa FOREIGN KEY (casa_rural_id) REFERENCES casa_rural(id),
    CONSTRAINT fk_reserva_huesped FOREIGN KEY (huesped_id) REFERENCES huesped(id)
);

-- 5. TABLA OPINION (Hija de Casa y Huesped) --
CREATE TABLE opinion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    puntuacion BIGINT,
    comentario VARCHAR(500),
    casa_rural_id BIGINT NOT NULL,
    huesped_id BIGINT NOT NULL,
    CONSTRAINT fk_opinion_casa FOREIGN KEY (casa_rural_id) REFERENCES casa_rural(id),
    CONSTRAINT fk_opinion_huesped FOREIGN KEY (huesped_id) REFERENCES huesped(id)
);

-- Crear la tabla 'users'
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    image VARCHAR(255),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    last_password_change_date TIMESTAMP
);

-- Crear la tabla 'roles'
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Crear la tabla 'user_roles'
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);
