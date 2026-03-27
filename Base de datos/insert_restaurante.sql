-- =====================================
-- CONSULTAS ÚTILES Y EJEMPLOS DE USO
-- Base de Datos Restaurante
-- =====================================

USE restaurante_db;

-- =====================================
-- EJEMPLOS DE INSERCIÓN DE DATOS
-- =====================================

-- Ejemplo: Crear un nuevo producto
INSERT INTO productos (nombre, descripcion, precio, categoria_id, tiempo_preparacion, es_preparado)
VALUES 
('Pizza Margarita', 'Pizza clásica con salsa de tomate, mozzarella y albahaca', 45.00, 4, 15, TRUE),
('Coca Cola 500ml', 'Bebida gaseosa', 8.00, 7, 0, FALSE);

select * from inventario

-- PASO 1: Crear proveedores primero
INSERT INTO proveedores (nombre, telefono, email, direccion, activo)
VALUES 
('Distribuidora Central', '555-1234', 'ventas@distcentral.com', 'Av. Principal 123', TRUE),
('Alimentos del Valle', '555-5678', 'info@alimentosvalle.com', 'Calle Comercio 456', TRUE),
('Carnes Premium', '555-9012', 'pedidos@carnespremium.com', 'Zona Industrial 789', TRUE);

-- PASO 2: Verificar que se crearon (opcional)
SELECT * FROM proveedores;

-- PASO 3: Ahora sí, agregar ingredientes
INSERT INTO inventario (nombre_ingrediente, unidad_medida, stock_actual, stock_minimo, stock_alerta, costo_unitario, proveedor_id)
VALUES 
('Harina de trigo', 'KG', 50.00, 10.00, 20.00, 2.50, 1),
('Queso mozzarella', 'KG', 15.00, 3.00, 5.00, 18.00, 1),
('Salsa de tomate', 'L', 10.00, 2.00, 5.00, 12.00, 1),
('Aceite de oliva', 'L', 8.00, 1.00, 3.00, 25.00, 1),
('Tomate fresco', 'KG', 20.00, 5.00, 10.00, 3.50, 1),
('Lechuga', 'UNIDAD', 30.00, 10.00, 15.00, 1.50, 1),
('Cebolla', 'KG', 15.00, 3.00, 8.00, 2.00, 1),
('Pollo', 'KG', 25.00, 5.00, 10.00, 12.00, 3),
('Carne de res', 'KG', 20.00, 5.00, 10.00, 25.00, 3),
('Pasta', 'KG', 40.00, 10.00, 20.00, 4.00, 2),
('Arroz', 'KG', 50.00, 15.00, 25.00, 3.00, 2),
('Sal', 'KG', 10.00, 2.00, 5.00, 1.00, 2),
('Azúcar', 'KG', 20.00, 5.00, 10.00, 2.50, 2);

-- Ver los IDs reales de tu inventario
SELECT id, nombre_ingrediente FROM inventario ORDER BY id;



INSERT INTO producto_ingrediente (producto_id, inventario_id, cantidad_usada)
SELECT 
    1,
    i.id,
    CASE 
        WHEN i.nombre_ingrediente = 'Harina de trigo' THEN 0.250
        WHEN i.nombre_ingrediente = 'Queso mozzarella' THEN 0.150
        WHEN i.nombre_ingrediente = 'Salsa de tomate' THEN 0.100
    END
FROM inventario i
WHERE i.nombre_ingrediente IN 
('Harina de trigo','Queso mozzarella','Salsa de tomate');


INSERT INTO stock_productos 
(producto_id, stock_actual, stock_minimo, stock_alerta, proveedor_id)
VALUES
(2, 50, 10, 20, 1);

INSERT INTO compras
(proveedor_id, numero_factura, subtotal, impuesto, total, metodo_pago, estado, usuario_id, notas)
VALUES
(1, 'FAC-001', 500.00, 50.00, 550.00, 'TRANSFERENCIA', 'RECIBIDA', 1, 'Compra mensual de insumos');

INSERT INTO detalle_compra
(compra_id, inventario_id, cantidad, precio_unitario, subtotal)
SELECT
    1,
    i.id,
    20,
    2.50,
    50.00
FROM inventario i
WHERE i.nombre_ingrediente = 'Harina de trigo';


select * from detalle_compra dc 

SELECT id FROM compras;

INSERT INTO movimientos_inventario
(inventario_id, tipo, cantidad, stock_anterior, stock_nuevo, referencia, motivo, usuario_id)
SELECT 
    i.id,
    'ENTRADA',
    20,
    i.stock_actual,
    i.stock_actual + 20,
    'COMPRA-1',
    'Compra proveedor',
    1
FROM inventario i
WHERE i.nombre_ingrediente = 'Harina de trigo';



SELECT id, nombre_ingrediente, stock_actual
FROM inventario;

INSERT INTO pedidos
(numero_pedido, mesa_id, usuario_id, cliente_nombre, tipo_pedido, estado, subtotal, total)
VALUES
('PED-001', 1, 1, 'Carlos Perez', 'MESA', 'SERVIDO', 53.00, 53.00);

INSERT INTO detalle_pedido
(pedido_id, producto_id, cantidad, precio_unitario, subtotal, notas, estado)
VALUES
(1, 1, 1, 45.00, 45.00, 'Sin cebolla', 'SERVIDO'),
(1, 2, 1, 8.00, 8.00, NULL, 'SERVIDO');

INSERT INTO facturas
(numero_factura, pedido_id, cliente_nombre, subtotal, impuesto, descuento, propina,
 monto_total, metodo_pago, monto_pagado, cambio, usuario_id)
VALUES
('F-001', 1, 'Carlos Perez', 53.00, 0.00, 0.00, 5.00,
 58.00, 'EFECTIVO', 60.00, 2.00, 1);

select * from facturas f 

INSERT INTO reservas
(mesa_id, cliente_nombre, cliente_telefono, cliente_email,
 fecha_reserva, cantidad_personas, estado, usuario_id)
VALUES
(2, 'Laura Gómez', '3001234567', 'laura@email.com',
 DATE_ADD(NOW(), INTERVAL 1 DAY), 4, 'CONFIRMADA', 1);

select * from reservas r 

INSERT INTO turnos_caja
(usuario_id, monto_inicial, total_ventas, total_efectivo,
 total_tarjeta, total_transferencia)
VALUES
(1, 200.00, 58.00, 58.00, 0.00, 0.00);


select * from categorias c 

-- =====================================
-- CONSULTAS DE OPERACIÓN DIARIA
-- =====================================

-- 1. Ver estado actual de todas las mesas
SELECT 
    m.numero,
    m.capacidad,
    m.zona,
    m.estado,
    p.numero_pedido,
    p.total,
    TIMESTAMPDIFF(MINUTE, p.fecha_creacion, NOW()) as minutos_ocupada,
    u.nombre as mesero
FROM mesas m
LEFT JOIN pedidos p ON m.id = p.mesa_id AND p.estado NOT IN ('PAGADO', 'CANCELADO')
LEFT JOIN usuarios u ON p.usuario_id = u.id
ORDER BY m.numero;

-- 2. Pedidos activos por estado
SELECT 
    p.numero_pedido,
    CASE 
        WHEN p.mesa_id IS NOT NULL THEN CONCAT('Mesa ', m.numero)
        ELSE p.tipo_pedido
    END AS ubicacion,
    p.estado,
    COUNT(dp.id) AS items,
    p.total,
    u.nombre AS mesero,
    TIMESTAMPDIFF(MINUTE, p.fecha_creacion, NOW()) AS tiempo_transcurrido
FROM pedidos p
LEFT JOIN mesas m ON p.mesa_id = m.id
LEFT JOIN detalle_pedido dp ON p.id = dp.pedido_id  -- ✅ CORRECCIÓN
LEFT JOIN usuarios u ON p.usuario_id = u.id
WHERE p.estado NOT IN ('PAGADO', 'CANCELADO')
GROUP BY p.id
ORDER BY p.fecha_creacion;

-- 3. Detalle de un pedido específico
SELECT 
    dp.id,
    pr.nombre as producto,
    dp.cantidad,
    dp.precio_unitario,
    dp.subtotal,
    dp.estado,
    dp.notas
FROM detalle_pedido dp
INNER JOIN productos pr ON dp.producto_id = pr.id
WHERE dp.pedido_id = 1
ORDER BY dp.id;

-- 4. Productos disponibles por categoría
SELECT 
    c.nombre as categoria,
    p.id,
    p.nombre,
    p.descripcion,
    p.precio,
    p.estado,
    p.tiempo_preparacion,
    CASE 
        WHEN p.es_preparado = TRUE THEN 'Preparado'
        ELSE CONCAT(IFNULL(sp.stock_actual, 0), ' disponibles')
    END as disponibilidad
FROM productos p
INNER JOIN categorias c ON p.categoria_id = c.id
LEFT JOIN stock_productos sp ON p.id = sp.producto_id
WHERE p.estado = 'ACTIVO'
ORDER BY c.orden_menu, p.nombre;

-- =====================================
-- CONSULTAS DE INVENTARIO
-- =====================================

-- 5. Stock actual de ingredientes
SELECT 
    i.nombre_ingrediente,
    i.stock_actual,
    i.stock_minimo,
    i.stock_alerta,
    i.unidad_medida,
    CASE 
        WHEN i.stock_actual <= i.stock_minimo THEN 'CRÍTICO'
        WHEN i.stock_actual <= i.stock_alerta THEN 'BAJO'
        ELSE 'OK'
    END as estado_stock,
    p.nombre as proveedor,
    p.telefono
FROM inventario i
LEFT JOIN proveedores p ON i.proveedor_id = p.id
ORDER BY 
    CASE 
        WHEN i.stock_actual <= i.stock_minimo THEN 1
        WHEN i.stock_actual <= i.stock_alerta THEN 2
        ELSE 3
    END,
    i.nombre_ingrediente;

-- 6. Historial de movimientos de un ingrediente
SELECT 
    m.fecha,
    m.tipo,
    m.cantidad,
    m.stock_anterior,
    m.stock_nuevo,
    m.referencia,
    m.motivo,
    u.nombre as usuario
FROM movimientos_inventario m
LEFT JOIN usuarios u ON m.usuario_id = u.id
WHERE m.inventario_id = 1
ORDER BY m.fecha DESC
LIMIT 50;

-- 7. Ingredientes próximos a vencer
SELECT 
    i.nombre_ingrediente,
    i.stock_actual,
    i.unidad_medida,
    i.fecha_vencimiento,
    DATEDIFF(i.fecha_vencimiento, CURDATE()) as dias_restantes,
    i.costo_unitario,
    (i.stock_actual * i.costo_unitario) as valor_inventario
FROM inventario i
WHERE i.fecha_vencimiento IS NOT NULL
    AND i.fecha_vencimiento <= DATE_ADD(CURDATE(), INTERVAL 7 DAY)
ORDER BY i.fecha_vencimiento;

-- 8. Costo de ingredientes por producto
SELECT 
    p.nombre as producto,
    SUM(pi.cantidad_usada * i.costo_unitario) as costo_ingredientes,
    p.precio as precio_venta,
    p.precio - SUM(pi.cantidad_usada * i.costo_unitario) as margen_bruto,
    ((p.precio - SUM(pi.cantidad_usada * i.costo_unitario)) / p.precio * 100) as margen_porcentaje
FROM productos p
INNER JOIN producto_ingrediente pi ON p.id = pi.producto_id
INNER JOIN inventario i ON pi.inventario_id = i.id
WHERE p.es_preparado = TRUE
GROUP BY p.id, p.nombre, p.precio
ORDER BY margen_porcentaje DESC;

-- =====================================
-- CONSULTAS DE VENTAS Y REPORTES
-- =====================================

-- 9. Ventas del día actual
SELECT 
    COUNT(f.id) as total_facturas,
    SUM(f.subtotal) as subtotal,
    SUM(f.impuesto) as impuestos,
    SUM(f.descuento) as descuentos,
    SUM(f.propina) as propinas,
    SUM(f.monto_total) as total_ventas,
    SUM(CASE WHEN f.metodo_pago = 'EFECTIVO' THEN f.monto_total ELSE 0 END) as efectivo,
    SUM(CASE WHEN f.metodo_pago IN ('TARJETA_DEBITO','TARJETA_CREDITO') THEN f.monto_total ELSE 0 END) as tarjeta,
    SUM(CASE WHEN f.metodo_pago = 'TRANSFERENCIA' THEN f.monto_total ELSE 0 END) as transferencia,
    AVG(f.monto_total) as ticket_promedio
FROM facturas f
WHERE DATE(f.fecha) = CURDATE()
    AND f.estado = 'EMITIDA';

-- 10. Ventas por categoría (hoy)
SELECT 
    c.nombre as categoria,
    COUNT(DISTINCT dp.pedido_id) as pedidos,
    SUM(dp.cantidad) as unidades_vendidas,
    SUM(dp.subtotal) as total_ventas,
    AVG(dp.precio_unitario) as precio_promedio
FROM detalle_pedido dp
INNER JOIN productos p ON dp.producto_id = p.id
INNER JOIN categorias c ON p.categoria_id = c.id
INNER JOIN pedidos ped ON dp.pedido_id = ped.id
WHERE DATE(ped.fecha_creacion) = CURDATE()
    AND ped.estado IN ('PAGADO', 'SERVIDO')
GROUP BY c.id, c.nombre
ORDER BY total_ventas DESC;

-- 11. Top 10 productos más vendidos (última semana)
SELECT 
    p.nombre as producto,
    c.nombre as categoria,
    SUM(dp.cantidad) as unidades_vendidas,
    SUM(dp.subtotal) as ingresos,
    COUNT(DISTINCT dp.pedido_id) as veces_pedido,
    AVG(dp.precio_unitario) as precio_promedio
FROM detalle_pedido dp
INNER JOIN productos p ON dp.producto_id = p.id
INNER JOIN categorias c ON p.categoria_id = c.id
INNER JOIN pedidos ped ON dp.pedido_id = ped.id
WHERE ped.fecha_creacion >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
    AND ped.estado IN ('PAGADO', 'SERVIDO')
GROUP BY p.id, p.nombre, c.nombre
ORDER BY unidades_vendidas DESC
LIMIT 10;

-- 12. Ventas por hora del día
SELECT 
    HOUR(p.fecha_creacion) as hora,
    COUNT(DISTINCT p.id) as cantidad_pedidos,
    SUM(p.total) as total_ventas,
    AVG(p.total) as ticket_promedio,
    SUM(CASE WHEN p.tipo_pedido = 'MESA' THEN 1 ELSE 0 END) as pedidos_mesa,
    SUM(CASE WHEN p.tipo_pedido = 'DOMICILIO' THEN 1 ELSE 0 END) as pedidos_domicilio,
    SUM(CASE WHEN p.tipo_pedido = 'PARA_LLEVAR' THEN 1 ELSE 0 END) as pedidos_llevar
FROM pedidos p
WHERE DATE(p.fecha_creacion) = CURDATE()
    AND p.estado IN ('PAGADO', 'SERVIDO')
GROUP BY HOUR(p.fecha_creacion)
ORDER BY hora;

-- 13. Rendimiento de meseros (hoy)
SELECT 
    u.nombre as mesero,
    COUNT(DISTINCT p.id) as pedidos_atendidos,
    COUNT(DISTINCT p.mesa_id) as mesas_atendidas,
    SUM(p.total) as total_ventas,
    AVG(p.total) as ticket_promedio,
    SUM(f.propina) as total_propinas,
    AVG(TIMESTAMPDIFF(MINUTE, p.fecha_creacion, f.fecha)) as tiempo_promedio_atencion
FROM pedidos p
INNER JOIN usuarios u ON p.usuario_id = u.id
LEFT JOIN facturas f ON p.id = f.pedido_id
WHERE DATE(p.fecha_creacion) = CURDATE()
    AND p.estado IN ('PAGADO', 'SERVIDO')
GROUP BY u.id, u.nombre
ORDER BY total_ventas DESC;

-- 14. Análisis de mesas más productivas
SELECT 
    m.numero as mesa,
    m.zona,
    m.capacidad,
    COUNT(p.id) as veces_usada,
    SUM(p.total) as total_generado,
    AVG(p.total) as ticket_promedio,
    AVG(TIMESTAMPDIFF(MINUTE, p.fecha_creacion, 
        CASE WHEN p.estado = 'PAGADO' THEN p.fecha_modificacion ELSE NULL END)) as tiempo_promedio_ocupacion
FROM mesas m
INNER JOIN pedidos p ON m.id = p.mesa_id
WHERE p.fecha_creacion >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
    AND p.estado = 'PAGADO'
GROUP BY m.id, m.numero, m.zona, m.capacidad
ORDER BY total_generado DESC;

-- =====================================
-- CONSULTAS DE COMPRAS
-- =====================================

-- 15. Compras del mes actual
SELECT 
    c.id,
    c.numero_factura,
    p.nombre as proveedor,
    c.fecha,
    c.total,
    c.estado,
    u.nombre as registrado_por,
    (SELECT COUNT(*) FROM detalle_compra WHERE compra_id = c.id) as items
FROM compras c
INNER JOIN proveedores p ON c.proveedor_id = p.id
LEFT JOIN usuarios u ON c.usuario_id = u.id
WHERE MONTH(c.fecha) = MONTH(CURDATE())
    AND YEAR(c.fecha) = YEAR(CURDATE())
ORDER BY c.fecha DESC;

-- 16. Detalle de una compra
SELECT 
    i.nombre_ingrediente,
    dc.cantidad,
    i.unidad_medida,
    dc.precio_unitario,
    dc.subtotal
FROM detalle_compra dc
INNER JOIN inventario i ON dc.inventario_id = i.id
WHERE dc.compra_id = 1;

-- 17. Gasto por proveedor (último trimestre)
SELECT 
    p.nombre as proveedor,
    COUNT(c.id) as compras_realizadas,
    SUM(c.total) as total_gastado,
    AVG(c.total) as promedio_compra,
    MAX(c.fecha) as ultima_compra
FROM proveedores p
INNER JOIN compras c ON p.id = c.proveedor_id
WHERE c.fecha >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH)
    AND c.estado != 'CANCELADA'
GROUP BY p.id, p.nombre
ORDER BY total_gastado DESC;

-- =====================================
-- CONSULTAS DE ANÁLISIS Y MÉTRICAS
-- =====================================

-- 18. Comparativa de ventas mes actual vs mes anterior
SELECT 
    'Mes Actual' as periodo,
    COUNT(f.id) as facturas,
    SUM(f.monto_total) as total_ventas,
    AVG(f.monto_total) as ticket_promedio
FROM facturas f
WHERE MONTH(f.fecha) = MONTH(CURDATE())
    AND YEAR(f.fecha) = YEAR(CURDATE())
    AND f.estado = 'EMITIDA'
UNION ALL
SELECT 
    'Mes Anterior' as periodo,
    COUNT(f.id) as facturas,
    SUM(f.monto_total) as total_ventas,
    AVG(f.monto_total) as ticket_promedio
FROM facturas f
WHERE MONTH(f.fecha) = MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))
    AND YEAR(f.fecha) = YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))
    AND f.estado = 'EMITIDA';

-- 19. Productos que nunca se han vendido
SELECT 
    p.id,
    p.nombre,
    c.nombre as categoria,
    p.precio,
    p.estado,
    p.fecha_creacion
FROM productos p
INNER JOIN categorias c ON p.categoria_id = c.id
LEFT JOIN detalle_pedido dp ON p.id = dp.producto_id
WHERE dp.id IS NULL
    AND p.estado = 'ACTIVO'
ORDER BY p.fecha_creacion DESC;

-- 20. Análisis ABC de productos (Pareto)
SELECT 
    p.nombre as producto,
    SUM(dp.subtotal) as ventas_totales,
    SUM(SUM(dp.subtotal)) OVER (ORDER BY SUM(dp.subtotal) DESC) as ventas_acumuladas,
    (SUM(SUM(dp.subtotal)) OVER (ORDER BY SUM(dp.subtotal) DESC) / 
     SUM(SUM(dp.subtotal)) OVER () * 100) as porcentaje_acumulado,
    CASE 
        WHEN (SUM(SUM(dp.subtotal)) OVER (ORDER BY SUM(dp.subtotal) DESC) / 
              SUM(SUM(dp.subtotal)) OVER () * 100) <= 80 THEN 'A'
        WHEN (SUM(SUM(dp.subtotal)) OVER (ORDER BY SUM(dp.subtotal) DESC) / 
              SUM(SUM(dp.subtotal)) OVER () * 100) <= 95 THEN 'B'
        ELSE 'C'
    END as clasificacion
FROM detalle_pedido dp
INNER JOIN productos p ON dp.producto_id = p.id
INNER JOIN pedidos ped ON dp.pedido_id = ped.id
WHERE ped.estado IN ('PAGADO', 'SERVIDO')
    AND ped.fecha_creacion >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY p.id, p.nombre
ORDER BY ventas_totales DESC;

-- =====================================
-- EJEMPLOS DE USO DE STORED PROCEDURES
-- =====================================

/*
-- Crear un nuevo pedido
CALL sp_crear_pedido(
    1,                    -- mesa_id
    2,                    -- usuario_id (mesero)
    'MESA',              -- tipo_pedido
    NULL,                -- cliente_nombre
    'Cliente prefiere vino tinto', -- notas
    @pedido_id           -- variable de salida
);
SELECT @pedido_id as nuevo_pedido_id;

-- Agregar items al pedido
CALL sp_agregar_item_pedido(@pedido_id, 1, 2, 'Sin cebolla'); -- 2 Pizzas
CALL sp_agregar_item_pedido(@pedido_id, 5, 1, NULL);          -- 1 Ensalada

-- Procesar el pago
CALL sp_procesar_pago(
    @pedido_id,          -- pedido_id
    'EFECTIVO',          -- metodo_pago
    'Juan Pérez',        -- cliente_nombre
    NULL,                -- cliente_ruc
    0,                   -- descuento
    5.00,                -- propina
    16,                  -- impuesto_porcentaje (16%)
    150.00,              -- monto_pagado
    2,                   -- usuario_id (cajero)
    @factura_id,         -- variable salida factura
    @cambio              -- variable salida cambio
);
SELECT @factura_id as factura_generada, @cambio as cambio_entregado;

-- Reporte de ventas
CALL sp_reporte_ventas('2025-02-01', '2025-02-28');

-- Top 5 productos más vendidos
CALL sp_productos_mas_vendidos('2025-02-01', '2025-02-28', 5);

-- Inventario con stock bajo
CALL sp_inventario_bajo_stock();

-- Cerrar turno de caja
CALL sp_cerrar_turno(1, 1250.50);

-- Cancelar un pedido
CALL sp_cancelar_pedido(10, 2, 'Cliente cambió de opinión');
*/

-- =====================================
-- FIN DE CONSULTAS ÚTILES
-- =====================================

USE restaurante_db;

-- Primero borramos el usuario admin y lo recreamos limpio


USE restaurante_db;

UPDATE usuarios 
SET password = '$2a$10$/fhlu73pPhl7XHypm2.vDOYvO7Ijr7s2zOXL4FALWz4h2TPoFF.EW'
WHERE email = 'admin@restaurante.com';

select * from usuarios u