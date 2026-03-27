-- =====================================
-- BASE DE DATOS RESTAURANTE - VERSIÓN COMPLETA
-- Incluye: Gestión de productos, inventario de ingredientes,
-- pedidos, facturación, compras y control de stock
-- =====================================

DROP DATABASE IF EXISTS restaurante_db;
CREATE DATABASE restaurante_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE restaurante_db;

-- =====================================
-- 1. ROLES Y PERMISOS
-- =====================================
CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

select*from roles;

-- =====================================
-- 2. USUARIOS (EMPLEADOS)
-- =====================================
CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    rol_id INT NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso DATETIME,
    FOREIGN KEY (rol_id) REFERENCES roles(id)
) ENGINE=InnoDB;

select*from usuarios;

-- =====================================
-- 3. CATEGORÍAS DE PRODUCTOS
-- =====================================
CREATE TABLE categorias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    orden_menu INT DEFAULT 0,
    activo BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB;

select*from categorias;

-- =====================================
-- 4. PRODUCTOS (MENÚ DEL RESTAURANTE)
-- =====================================
CREATE TABLE productos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL,
    categoria_id INT NOT NULL,
    imagen_url VARCHAR(255),
    tiempo_preparacion INT COMMENT 'Tiempo en minutos',
    estado ENUM('ACTIVO','INACTIVO','AGOTADO') DEFAULT 'ACTIVO',
    es_preparado BOOLEAN DEFAULT TRUE COMMENT 'TRUE si se prepara, FALSE si es producto empacado',
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id)
) ENGINE=InnoDB;

select*from productos;
-- =====================================
-- 5. MESAS
-- =====================================
CREATE TABLE mesas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero INT NOT NULL UNIQUE,
    capacidad INT NOT NULL,
    zona VARCHAR(50) COMMENT 'Interior, Terraza, VIP, Bar',
    descripcion VARCHAR(100),
    estado ENUM('LIBRE','OCUPADA','RESERVADA','FUERA_SERVICIO') DEFAULT 'LIBRE',
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

select*from mesas;
-- =====================================
-- 6. PROVEEDORES
-- =====================================
CREATE TABLE proveedores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    razon_social VARCHAR(200),
    ruc_nit VARCHAR(50),
    telefono VARCHAR(50),
    email VARCHAR(100),
    direccion VARCHAR(255),
    contacto_nombre VARCHAR(100),
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

select*from proveedores;
-- =====================================
-- 7. INVENTARIO (INGREDIENTES Y MATERIA PRIMA)
-- =====================================
CREATE TABLE inventario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_ingrediente VARCHAR(100) NOT NULL,
    descripcion TEXT,
    unidad_medida ENUM('KG','G','L','ML','UNIDAD','PORCION') NOT NULL,
    stock_actual DECIMAL(10,2) NOT NULL DEFAULT 0,
    stock_minimo DECIMAL(10,2) NOT NULL DEFAULT 5,
    stock_alerta DECIMAL(10,2) NOT NULL DEFAULT 10,
    costo_unitario DECIMAL(10,2) DEFAULT 0,
    proveedor_id BIGINT,
    ubicacion VARCHAR(100) COMMENT 'Refrigerador, Almacén, etc',
    fecha_vencimiento DATE,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (proveedor_id) REFERENCES proveedores(id)
) ENGINE=InnoDB;

select*from inventario;
-- =====================================
-- 8. RECETAS (INGREDIENTES POR PRODUCTO)
-- =====================================
CREATE TABLE producto_ingrediente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    inventario_id BIGINT NOT NULL,
    cantidad_usada DECIMAL(10,2) NOT NULL COMMENT 'Cantidad en la unidad del ingrediente',
    notas TEXT,
    FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE,
    FOREIGN KEY (inventario_id) REFERENCES inventario(id),
    UNIQUE KEY unique_producto_ingrediente (producto_id, inventario_id)
) ENGINE=InnoDB;

select*from producto_ingrediente t ;

-- =====================================aqui
-- 9. STOCK PRODUCTOS EMPACADOS (OPCIONAL)
-- Para bebidas embotelladas, postres pre-empacados, etc.
-- =====================================
CREATE TABLE stock_productos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    stock_actual INT NOT NULL DEFAULT 0,
    stock_minimo INT DEFAULT 5,
    stock_alerta INT DEFAULT 10,
    proveedor_id BIGINT,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (producto_id) REFERENCES productos(id),
    FOREIGN KEY (proveedor_id) REFERENCES proveedores(id),
    UNIQUE KEY unique_producto_stock (producto_id)
) ENGINE=InnoDB;

select*from stock_productos sp ;
-- =====================================
-- 10. COMPRAS A PROVEEDORES
-- =====================================
CREATE TABLE compras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    proveedor_id BIGINT NOT NULL,
    numero_factura VARCHAR(50),
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(10,2) NOT NULL,
    impuesto DECIMAL(10,2) DEFAULT 0,
    total DECIMAL(10,2) NOT NULL,
    metodo_pago ENUM('EFECTIVO','TRANSFERENCIA','CREDITO') DEFAULT 'EFECTIVO',
    estado ENUM('PENDIENTE','RECIBIDA','CANCELADA') DEFAULT 'PENDIENTE',
    usuario_id INT NOT NULL COMMENT 'Usuario que registró la compra',
    notas TEXT,
    FOREIGN KEY (proveedor_id) REFERENCES proveedores(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

select*from compras c ;
-- =====================================
-- 11. DETALLE DE COMPRAS
-- =====================================
CREATE TABLE detalle_compra (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    compra_id BIGINT NOT NULL,
    inventario_id BIGINT NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (compra_id) REFERENCES compras(id) ON DELETE CASCADE,
    FOREIGN KEY (inventario_id) REFERENCES inventario(id)
) ENGINE=InnoDB;

select*from detalle_compra dc ;
-- =====================================
-- 12. MOVIMIENTOS DE INVENTARIO
-- =====================================
CREATE TABLE movimientos_inventario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inventario_id BIGINT NOT NULL,
    tipo ENUM('ENTRADA','SALIDA','AJUSTE','MERMA') NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    stock_anterior DECIMAL(10,2) NOT NULL,
    stock_nuevo DECIMAL(10,2) NOT NULL,
    referencia VARCHAR(100) COMMENT 'ID de pedido, compra, etc',
    motivo VARCHAR(255),
    usuario_id INT,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (inventario_id) REFERENCES inventario(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

select*from movimientos_inventario mi ;
-- =====================================
-- 13. PEDIDOS
-- =====================================
CREATE TABLE pedidos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_pedido VARCHAR(20) UNIQUE COMMENT 'Número visible para el cliente',
    mesa_id INT,
    usuario_id INT NOT NULL COMMENT 'Mesero o cajero que tomó el pedido',
    cliente_nombre VARCHAR(100),
    cliente_telefono VARCHAR(20),
    cliente_direccion TEXT,
    tipo_pedido ENUM('MESA','DOMICILIO','PARA_LLEVAR') DEFAULT 'MESA',
    estado ENUM('PENDIENTE','EN_PREPARACION','LISTO','SERVIDO','PAGADO','CANCELADO') DEFAULT 'PENDIENTE',
    subtotal DECIMAL(10,2) DEFAULT 0.00,
    total DECIMAL(10,2) DEFAULT 0.00,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_preparacion DATETIME,
    fecha_entrega DATETIME,
    notas_especiales TEXT,
    modificado_por INT,
    fecha_modificacion DATETIME,
    FOREIGN KEY (mesa_id) REFERENCES mesas(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (modificado_por) REFERENCES usuarios(id)
) ENGINE=InnoDB;

select * from pedidos p 

-- =====================================
-- 14. DETALLE DE PEDIDOS
-- =====================================
CREATE TABLE detalle_pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL COMMENT 'Precio al momento del pedido',
    subtotal DECIMAL(10,2) NOT NULL,
    notas TEXT COMMENT 'Sin cebolla, extra queso, etc',
    estado ENUM('PENDIENTE','EN_PREPARACION','LISTO','SERVIDO') DEFAULT 'PENDIENTE',
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES productos(id)
) ENGINE=InnoDB;

select * from detalle_pedido dp 

-- =====================================
-- 15. FACTURAS
-- =====================================
CREATE TABLE facturas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_factura VARCHAR(50) UNIQUE NOT NULL,
    pedido_id BIGINT NOT NULL,
    cliente_nombre VARCHAR(100),
    cliente_ruc_nit VARCHAR(50),
    subtotal DECIMAL(10,2) NOT NULL,
    impuesto DECIMAL(10,2) DEFAULT 0.00,
    descuento DECIMAL(10,2) DEFAULT 0.00,
    propina DECIMAL(10,2) DEFAULT 0.00,
    monto_total DECIMAL(10,2) NOT NULL,
    metodo_pago ENUM('EFECTIVO','TARJETA_DEBITO','TARJETA_CREDITO','TRANSFERENCIA','QR','MIXTO') NOT NULL,
    monto_pagado DECIMAL(10,2),
    cambio DECIMAL(10,2),
    estado ENUM('EMITIDA','ANULADA') DEFAULT 'EMITIDA',
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    usuario_id INT NOT NULL COMMENT 'Usuario que emitió la factura',
    modificado_por INT,
    fecha_modificacion DATETIME,
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (modificado_por) REFERENCES usuarios(id)
) ENGINE=InnoDB;

select * from facturas f 

-- =====================================
-- 16. RESERVAS (OPCIONAL PERO ÚTIL)
-- =====================================
CREATE TABLE reservas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mesa_id INT NOT NULL,
    cliente_nombre VARCHAR(100) NOT NULL,
    cliente_telefono VARCHAR(20) NOT NULL,
    cliente_email VARCHAR(100),
    fecha_reserva DATETIME NOT NULL,
    cantidad_personas INT NOT NULL,
    estado ENUM('CONFIRMADA','CANCELADA','COMPLETADA','NO_ASISTIO') DEFAULT 'CONFIRMADA',
    notas TEXT,
    usuario_id INT COMMENT 'Usuario que registró la reserva',
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (mesa_id) REFERENCES mesas(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

drop table reservas 

-- =====================================
-- 17. TURNOS/CAJAS
-- =====================================
CREATE TABLE turnos_caja (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    fecha_apertura DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre DATETIME,
    monto_inicial DECIMAL(10,2) NOT NULL,
    monto_final DECIMAL(10,2),
    total_ventas DECIMAL(10,2) DEFAULT 0,
    total_efectivo DECIMAL(10,2) DEFAULT 0,
    total_tarjeta DECIMAL(10,2) DEFAULT 0,
    total_transferencia DECIMAL(10,2) DEFAULT 0,
    diferencia DECIMAL(10,2) DEFAULT 0,
    estado ENUM('ABIERTO','CERRADO') DEFAULT 'ABIERTO',
    notas TEXT,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

select * from turnos_caja tc 
-- =====================================
-- ÍNDICES PARA OPTIMIZACIÓN
-- =====================================

-- Índices para pedidos
CREATE INDEX idx_pedidos_fecha ON pedidos(fecha_creacion);
CREATE INDEX idx_pedidos_estado ON pedidos(estado);
CREATE INDEX idx_pedidos_mesa ON pedidos(mesa_id);
CREATE INDEX idx_pedidos_usuario ON pedidos(usuario_id);
CREATE INDEX idx_pedidos_tipo ON pedidos(tipo_pedido);

-- Índices para detalle pedido
CREATE INDEX idx_detalle_pedido_pedido ON detalle_pedido(pedido_id);
CREATE INDEX idx_detalle_pedido_producto ON detalle_pedido(producto_id);

-- Índices para productos
CREATE INDEX idx_productos_categoria ON productos(categoria_id);
CREATE INDEX idx_productos_estado ON productos(estado);

-- Índices para inventario
CREATE INDEX idx_inventario_proveedor ON inventario(proveedor_id);
CREATE INDEX idx_inventario_stock ON inventario(stock_actual);

-- Índices para movimientos
CREATE INDEX idx_movimientos_inventario ON movimientos_inventario(inventario_id);
CREATE INDEX idx_movimientos_fecha ON movimientos_inventario(fecha);
CREATE INDEX idx_movimientos_tipo ON movimientos_inventario(tipo);

-- Índices para compras
CREATE INDEX idx_compras_proveedor ON compras(proveedor_id);
CREATE INDEX idx_compras_fecha ON compras(fecha);
CREATE INDEX idx_compras_estado ON compras(estado);

-- Índices para facturas
CREATE INDEX idx_facturas_fecha ON facturas(fecha);
CREATE INDEX idx_facturas_pedido ON facturas(pedido_id);
CREATE INDEX idx_facturas_estado ON facturas(estado);

-- Índices para reservas
CREATE INDEX idx_reservas_mesa ON reservas(mesa_id);
CREATE INDEX idx_reservas_fecha ON reservas(fecha_reserva);
CREATE INDEX idx_reservas_estado ON reservas(estado);

-- =====================================
-- DATOS INICIALES
-- =====================================

-- Roles básicos
INSERT INTO roles (nombre, descripcion) VALUES 
('ADMIN', 'Administrador del sistema'),
('GERENTE', 'Gerente del restaurante'),
('MESERO', 'Mesero/Camarero'),
('COCINERO', 'Personal de cocina'),
('CAJERO', 'Cajero'),
('BARTENDER', 'Bartender/Barman');

-- Usuario administrador por defecto (contraseña: admin123 - cambiar en producción)
INSERT INTO usuarios (nombre, email, password, rol_id) VALUES 
('Administrador', 'admin@restaurante.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1);

-- Categorías ejemplo
INSERT INTO categorias (nombre, descripcion, orden_menu) VALUES 
('Entradas', 'Aperitivos y entradas', 1),
('Platos Principales', 'Platos fuertes', 2),
('Pastas', 'Pastas y risottos', 3),
('Pizzas', 'Pizzas artesanales', 4),
('Ensaladas', 'Ensaladas frescas', 5),
('Postres', 'Postres caseros', 6),
('Bebidas', 'Bebidas frías y calientes', 7),
('Cócteles', 'Cócteles de la casa', 8);

-- Mesas ejemplo
INSERT INTO mesas (numero, capacidad, zona) VALUES 
(1, 2, 'Interior'),
(2, 2, 'Interior'),
(3, 4, 'Interior'),
(4, 4, 'Interior'),
(5, 6, 'Interior'),
(6, 4, 'Terraza'),
(7, 4, 'Terraza'),
(8, 2, 'Terraza'),
(9, 8, 'VIP'),
(10, 2, 'Bar');

-- =====================================
-- VISTAS ÚTILES
-- =====================================

-- Vista de productos con stock bajo
CREATE VIEW v_stock_bajo AS
SELECT 
    i.id,
    i.nombre_ingrediente,
    i.stock_actual,
    i.stock_alerta,
    i.unidad_medida,
    p.nombre as proveedor,
    i.costo_unitario,
    (i.stock_alerta - i.stock_actual) as cantidad_a_pedir
FROM inventario i
LEFT JOIN proveedores p ON i.proveedor_id = p.id
WHERE i.stock_actual <= i.stock_alerta;

-- Vista de ventas del día
CREATE VIEW v_ventas_dia AS
SELECT 
    DATE(f.fecha) as fecha,
    COUNT(f.id) as total_facturas,
    SUM(f.subtotal) as subtotal,
    SUM(f.impuesto) as impuestos,
    SUM(f.descuento) as descuentos,
    SUM(f.propina) as propinas,
    SUM(f.monto_total) as total_ventas,
    SUM(CASE WHEN f.metodo_pago = 'EFECTIVO' THEN f.monto_total ELSE 0 END) as efectivo,
    SUM(CASE WHEN f.metodo_pago IN ('TARJETA_DEBITO','TARJETA_CREDITO') THEN f.monto_total ELSE 0 END) as tarjeta,
    SUM(CASE WHEN f.metodo_pago = 'TRANSFERENCIA' THEN f.monto_total ELSE 0 END) as transferencia
FROM facturas f
WHERE f.estado = 'EMITIDA'
GROUP BY DATE(f.fecha);

-- Vista de productos más vendidos
CREATE VIEW v_productos_mas_vendidos AS
SELECT 
    p.id,
    p.nombre,
    c.nombre as categoria,
    SUM(dp.cantidad) as total_vendido,
    SUM(dp.subtotal) as ingresos_generados,
    COUNT(DISTINCT dp.pedido_id) as veces_pedido
FROM productos p
INNER JOIN detalle_pedido dp ON p.id = dp.producto_id
INNER JOIN categorias c ON p.categoria_id = c.id
INNER JOIN pedidos ped ON dp.pedido_id = ped.id
WHERE ped.estado IN ('PAGADO','SERVIDO')
GROUP BY p.id, p.nombre, c.nombre
ORDER BY total_vendido DESC;

-- Vista de estado de mesas
CREATE VIEW v_estado_mesas AS
SELECT 
    m.id,
    m.numero,
    m.capacidad,
    m.zona,
    m.estado,
    p.id as pedido_id,
    p.numero_pedido,
    p.total as total_pedido,
    u.nombre as mesero
FROM mesas m
LEFT JOIN pedidos p ON m.id = p.mesa_id AND p.estado NOT IN ('PAGADO','CANCELADO')
LEFT JOIN usuarios u ON p.usuario_id = u.id;


ALTER TABLE facturas
ADD turno_id BIGINT,
ADD FOREIGN KEY (turno_id) REFERENCES turnos_caja(id);

ALTER TABLE facturas
ADD UNIQUE (pedido_id);


CREATE TABLE clientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion TEXT,
    ruc_nit VARCHAR(50),
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE
);

SELECT * FROM clientes;

INSERT INTO clientes (nombre, telefono, email, direccion, ruc_nit) VALUES
('Juan Pérez', '3001234567', 'juan@gmail.com', 'Cra 45 #23-10, Medellín', NULL),
('María Gómez', '3109876543', 'maria@gmail.com', 'Cll 50 #30-20, Medellín', NULL),
('Empresa XYZ', '6041234567', 'compras@xyz.com', 'Cra 70 #15-30, Medellín', '900123456-1'),
('Carlos Ruiz', '3205556677', NULL, 'Cll 10 #5-40, Envigado', NULL),
('Laura Torres', '3154443322', 'laura@hotmail.com', 'Av El Poblado #1-20, Medellín', NULL);

ALTER TABLE pedidos
ADD COLUMN cliente_id BIGINT,
ADD FOREIGN KEY (cliente_id) REFERENCES clientes(id);

ALTER TABLE facturas
ADD COLUMN cliente_id BIGINT,
ADD FOREIGN KEY (cliente_id) REFERENCES clientes(id);

SELECT * FROM clientes;
use restaurante_db;
ALTER TABLE reservas
ADD COLUMN total_estimado DECIMAL(10,2),
ADD COLUMN anticipo DECIMAL(10,2);



-- =====================================
-- FIN DEL SCRIPT
-- =====================================

