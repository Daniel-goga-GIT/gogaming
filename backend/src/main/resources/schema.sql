-- =========================
-- TABLA USUARIOS
-- =========================
CREATE TABLE IF NOT EXISTS usuarios (
    idusuario BIGINT PRIMARY KEY AUTO_INCREMENT,
    correo_electronico VARCHAR(100) UNIQUE NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    nombre VARCHAR(50) NOT NULL,
    apellidos VARCHAR(50) NOT NULL,
    code VARCHAR(10),
    image VARCHAR(255),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_password_change_date TIMESTAMP
);

-- =========================
-- TABLA ROLES
-- =========================
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- =========================
-- TABLA USER_ROLES (M2M)
-- =========================
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES usuarios(idusuario) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- =========================
-- TABLA PRODUCTOS BASE
-- =========================
CREATE TABLE IF NOT EXISTS productos (
    idproducto BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100),
    marca VARCHAR(50),
    fecha_publicacion DATE,
    precio DECIMAL(10,2),
    stock INT,
    tipo_producto ENUM('Raton', 'Teclado', 'Skate', 'Alfombrilla', 'Auriculares', 'Manga'),
    image VARCHAR(255)
);

-- =========================
-- RATONES
-- =========================
CREATE TABLE IF NOT EXISTS raton (
    idproducto BIGINT PRIMARY KEY,
    tipo_agarre ENUM('Fingertip', 'Claw', 'Palm'),
    medidas VARCHAR(50),
    sensor VARCHAR(50),
    peso DECIMAL(5,2),
    switches VARCHAR(50),
    FOREIGN KEY (idproducto) REFERENCES productos(idproducto) ON DELETE CASCADE
);

-- =========================
-- SKATES (pies de raton)
-- =========================
CREATE TABLE IF NOT EXISTS skate (
    idproducto BIGINT PRIMARY KEY,
    material VARCHAR(100),
    grosor DECIMAL(5,2),
    compatibilidad VARCHAR(255),
    FOREIGN KEY (idproducto) REFERENCES productos(idproducto) ON DELETE CASCADE
);

-- =========================
-- TECLADOS
-- =========================
CREATE TABLE IF NOT EXISTS teclado (
    idproducto BIGINT PRIMARY KEY,
    tipo_switch VARCHAR(100),
    layout VARCHAR(50),
    conectividad VARCHAR(50),
    retroiluminacion VARCHAR(50),
    FOREIGN KEY (idproducto) REFERENCES productos(idproducto) ON DELETE CASCADE
);

-- =========================
-- ALFOMBRILLAS
-- =========================
CREATE TABLE IF NOT EXISTS alfombrilla (
    idproducto BIGINT PRIMARY KEY,
    dimensiones VARCHAR(100),
    grosor DECIMAL(5,2),
    superficie VARCHAR(100),
    FOREIGN KEY (idproducto) REFERENCES productos(idproducto) ON DELETE CASCADE
);

-- =========================
-- AURICULARES
-- =========================
CREATE TABLE IF NOT EXISTS auriculares (
    idproducto BIGINT PRIMARY KEY,
    tipo VARCHAR(50),
    driver VARCHAR(100),
    conectividad VARCHAR(100),
    impedancia VARCHAR(50),
    FOREIGN KEY (idproducto) REFERENCES productos(idproducto) ON DELETE CASCADE
);

-- =========================
-- MANGAS
-- =========================
CREATE TABLE IF NOT EXISTS manga (
    idproducto BIGINT PRIMARY KEY,
    talla VARCHAR(20),
    material VARCHAR(100),
    largo DECIMAL(5,2),
    color VARCHAR(50),
    FOREIGN KEY (idproducto) REFERENCES productos(idproducto) ON DELETE CASCADE
);

-- =========================
-- TICKETS (PEDIDOS)
-- =========================
CREATE TABLE IF NOT EXISTS tickets (
    idticket BIGINT PRIMARY KEY AUTO_INCREMENT,
    idusuario BIGINT,
    numero_ticket VARCHAR(20) UNIQUE NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10,2) NOT NULL,
    metodo_pago ENUM('Tarjeta', 'PayPal', 'Transferencia') NOT NULL,
    estado ENUM('Preparando', 'Enviado', 'Entregado', 'Cancelado') NOT NULL DEFAULT 'Preparando',
    stripe_session_id VARCHAR(255) NULL UNIQUE,
    FOREIGN KEY (idusuario) REFERENCES usuarios(idusuario) ON DELETE SET NULL
);

-- Migración para BDs creadas antes de integrar Stripe
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS stripe_session_id VARCHAR(255) NULL UNIQUE;

-- =========================
-- LINEAS DE TICKET
-- =========================
CREATE TABLE IF NOT EXISTS lineas_ticket (
    idlinea BIGINT PRIMARY KEY AUTO_INCREMENT,
    idticket BIGINT NOT NULL,
    idproducto BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (idticket) REFERENCES tickets(idticket) ON DELETE CASCADE,
    FOREIGN KEY (idproducto) REFERENCES productos(idproducto)
);

-- =========================
-- RESENAS DE PRODUCTO
-- =========================
CREATE TABLE IF NOT EXISTS resenas (
    idresena BIGINT PRIMARY KEY AUTO_INCREMENT,
    idusuario BIGINT NOT NULL,
    idproducto BIGINT NOT NULL,
    calificacion INT NOT NULL,
    contenido TEXT NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_resenas_calificacion CHECK (calificacion BETWEEN 1 AND 5),
    FOREIGN KEY (idusuario) REFERENCES usuarios(idusuario) ON DELETE CASCADE,
    FOREIGN KEY (idproducto) REFERENCES productos(idproducto) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_resenas_producto ON resenas (idproducto);
CREATE INDEX IF NOT EXISTS idx_resenas_fecha ON resenas (fecha_creacion);
