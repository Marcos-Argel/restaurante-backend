-- 1. TABLAS BASE (Sin dependencias)
CREATE TABLE roles (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       nombre VARCHAR(50) NOT NULL UNIQUE,
                       descripcion VARCHAR(255),
                       fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE categorias (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            nombre VARCHAR(100) NOT NULL UNIQUE,
                            descripcion TEXT,
                            orden_menu INT DEFAULT 0,
                            activo BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE mesas (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       numero INT NOT NULL UNIQUE,
                       capacidad INT NOT NULL,
                       zona VARCHAR(50),
                       descripcion VARCHAR(100),
                       estado ENUM('LIBRE','OCUPADA','RESERVADA','FUERA_SERVICIO') DEFAULT 'LIBRE',
                       fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

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

-- 2. TABLAS CON DEPENDENCIAS DE NIVEL 1
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

CREATE TABLE productos (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           nombre VARCHAR(150) NOT NULL,
                           descripcion TEXT,
                           precio DECIMAL(10,2) NOT NULL,
                           categoria_id INT NOT NULL,
                           imagen_url VARCHAR(255),
                           tiempo_preparacion INT,
                           estado ENUM('ACTIVO','INACTIVO','AGOTADO') DEFAULT 'ACTIVO',
                           es_preparado BOOLEAN DEFAULT TRUE,
                           fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
                           fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           FOREIGN KEY (categoria_id) REFERENCES categorias(id)
) ENGINE=InnoDB;

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
                            ubicacion VARCHAR(100),
                            fecha_vencimiento DATE,
                            fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            FOREIGN KEY (proveedor_id) REFERENCES proveedores(id)
) ENGINE=InnoDB;

-- 3. TURNOS (Debe existir para que Facturas pueda referenciarlo)
CREATE TABLE turnos_caja (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             diferencia DECIMAL(10, 2) DEFAULT 0.00,
                             estado ENUM('ABIERTO', 'CERRADO') DEFAULT 'ABIERTO',
                             fecha_apertura DATETIME DEFAULT CURRENT_TIMESTAMP,
                             fecha_cierre DATETIME,
                             monto_final DECIMAL(10, 2),
                             monto_inicial DECIMAL(10, 2) NOT NULL,
                             notas TEXT,
                             total_efectivo DECIMAL(10, 2) DEFAULT 0.00,
                             total_tarjeta DECIMAL(10, 2) DEFAULT 0.00,
                             total_transferencia DECIMAL(10, 2) DEFAULT 0.00,
                             total_ventas DECIMAL(10, 2) DEFAULT 0.00,
                             usuario_id INT NOT NULL,
                             FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE = InnoDB;

-- 4. OPERACIONES (Pedidos, Compras, etc.)
CREATE TABLE pedidos (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         numero_pedido VARCHAR(20) UNIQUE,
                         mesa_id INT,
                         usuario_id INT NOT NULL,
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

CREATE TABLE facturas (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          numero_factura VARCHAR(50) UNIQUE NOT NULL,
                          pedido_id BIGINT NOT NULL,
                          turno_id BIGINT NOT NULL,
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
                          usuario_id INT NOT NULL,
                          modificado_por INT,
                          fecha_modificacion DATETIME,
                          FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
                          FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
                          FOREIGN KEY (turno_id) REFERENCES turnos_caja(id),
                          FOREIGN KEY (modificado_por) REFERENCES usuarios(id)
) ENGINE=InnoDB;

-- 5. OTRAS TABLAS Y DETALLES
CREATE TABLE producto_ingrediente (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      producto_id BIGINT NOT NULL,
                                      inventario_id BIGINT NOT NULL,
                                      cantidad_usada DECIMAL(10,2) NOT NULL,
                                      notas TEXT,
                                      FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE,
                                      FOREIGN KEY (inventario_id) REFERENCES inventario(id),
                                      UNIQUE KEY unique_producto_ingrediente (producto_id, inventario_id)
) ENGINE=InnoDB;

CREATE TABLE detalle_pedido (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                pedido_id BIGINT NOT NULL,
                                producto_id BIGINT NOT NULL,
                                cantidad INT NOT NULL,
                                precio_unitario DECIMAL(10,2) NOT NULL,
                                subtotal DECIMAL(10,2) NOT NULL,
                                notas TEXT,
                                estado ENUM('PENDIENTE','EN_PREPARACION','LISTO','SERVIDO') DEFAULT 'PENDIENTE',
                                FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
                                FOREIGN KEY (producto_id) REFERENCES productos(id)
) ENGINE=InnoDB;

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
                          usuario_id INT,
                          fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
                          anticipo DECIMAL(10,2),
                          total_estimado DECIMAL(10,2),
                          FOREIGN KEY (mesa_id) REFERENCES mesas(id),
                          FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

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
-- COMPRAS
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


-- Indices para pedidos
CREATE INDEX idx_pedidos_fecha ON pedidos(fecha_creacion);
CREATE INDEX idx_pedidos_estado ON pedidos(estado);
CREATE INDEX idx_pedidos_mesa ON pedidos(mesa_id);
CREATE INDEX idx_pedidos_usuario ON pedidos(usuario_id);
CREATE INDEX idx_pedidos_tipo ON pedidos(tipo_pedido);

-- Indices para detalle pedido
CREATE INDEX idx_detalle_pedido_pedido ON detalle_pedido(pedido_id);
CREATE INDEX idx_detalle_pedido_producto ON detalle_pedido(producto_id);

-- Indices para productos
CREATE INDEX idx_productos_categoria ON productos(categoria_id);
CREATE INDEX idx_productos_estado ON productos(estado);

-- Indices para inventario
CREATE INDEX idx_inventario_proveedor ON inventario(proveedor_id);
CREATE INDEX idx_inventario_stock ON inventario(stock_actual);

-- Indices para movimientos
CREATE INDEX idx_movimientos_inventario ON movimientos_inventario(inventario_id);
CREATE INDEX idx_movimientos_fecha ON movimientos_inventario(fecha);
CREATE INDEX idx_movimientos_tipo ON movimientos_inventario(tipo);

-- Indices para compras
CREATE INDEX idx_compras_proveedor ON compras(proveedor_id);
CREATE INDEX idx_compras_fecha ON compras(fecha);
CREATE INDEX idx_compras_estado ON compras(estado);

-- Indices para facturas
CREATE INDEX idx_facturas_fecha ON facturas(fecha);
CREATE INDEX idx_facturas_pedido ON facturas(pedido_id);
CREATE INDEX idx_facturas_estado ON facturas(estado);

-- Indices para reservas
CREATE INDEX idx_reservas_mesa ON reservas(mesa_id);
CREATE INDEX idx_reservas_fecha ON reservas(fecha_reserva);
CREATE INDEX idx_reservas_estado ON reservas(estado);