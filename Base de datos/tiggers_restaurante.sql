DELIMITER $$

DROP TRIGGER IF EXISTS trg_pedido_descontar_inventario $$

CREATE TRIGGER trg_pedido_descontar_inventario
AFTER UPDATE ON pedidos
FOR EACH ROW
BEGIN
    DECLARE done INT DEFAULT FALSE;

    DECLARE v_producto_id INT;
    DECLARE v_cantidad_producto DECIMAL(10,2);

    DECLARE v_ingrediente_id INT;
    DECLARE v_cantidad_necesaria DECIMAL(10,2);

    -- Cursor para productos del pedido
    DECLARE cur_productos CURSOR FOR
        SELECT producto_id, cantidad
        FROM detalle_pedido
        WHERE pedido_id = NEW.id;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    -- Solo ejecutar si cambia de estado
    IF NEW.estado = 'EN_PREPARACION' AND OLD.estado = 'PENDIENTE' THEN

        OPEN cur_productos;

        productos_loop: LOOP
            FETCH cur_productos INTO v_producto_id, v_cantidad_producto;

            IF done THEN
                LEAVE productos_loop;
            END IF;

            -- Descontar ingredientes del producto
            BEGIN
                DECLARE done2 INT DEFAULT FALSE;

                DECLARE cur_ingredientes CURSOR FOR
                    SELECT ingrediente_id, cantidad
                    FROM producto_ingrediente
                    WHERE producto_id = v_producto_id;

                DECLARE CONTINUE HANDLER FOR NOT FOUND SET done2 = TRUE;

                OPEN cur_ingredientes;

                ingredientes_loop: LOOP
                    FETCH cur_ingredientes INTO v_ingrediente_id, v_cantidad_necesaria;

                    IF done2 THEN
                        LEAVE ingredientes_loop;
                    END IF;

                    -- Multiplicar por cantidad pedida
                    SET v_cantidad_necesaria = v_cantidad_necesaria * v_cantidad_producto;

                    -- Validar stock
                    IF (
                        SELECT stock_actual
                        FROM inventario
                        WHERE ingrediente_id = v_ingrediente_id
                    ) < v_cantidad_necesaria THEN

                        SIGNAL SQLSTATE '45000'
                        SET MESSAGE_TEXT = 'Stock insuficiente para procesar el pedido';

                    END IF;

                    -- Descontar inventario
                    UPDATE inventario
                    SET stock_actual = stock_actual - v_cantidad_necesaria
                    WHERE ingrediente_id = v_ingrediente_id;

                    -- Registrar movimiento
                    INSERT INTO movimientos_inventario
                    (inventario_id, tipo, cantidad, stock_anterior, stock_nuevo, referencia, motivo)
                    SELECT 
                        id,
                        'SALIDA',
                        v_cantidad_necesaria,
                        stock_actual + v_cantidad_necesaria,
                        stock_actual,
                        NEW.numero_pedido,
                        'Venta de producto'
                    FROM inventario
                    WHERE ingrediente_id = v_ingrediente_id;

                END LOOP;

                CLOSE cur_ingredientes;
            END;

        END LOOP;

        CLOSE cur_productos;

    END IF;

END$$

DELIMITER ;


DELIMITER $$

DROP PROCEDURE IF EXISTS sp_cancelar_pedido $$

CREATE PROCEDURE sp_cancelar_pedido(IN p_pedido_id INT)
BEGIN
    DECLARE done INT DEFAULT FALSE;

    DECLARE v_producto_id INT;
    DECLARE v_cantidad_producto DECIMAL(10,2);

    DECLARE v_ingrediente_id INT;
    DECLARE v_cantidad_necesaria DECIMAL(10,2);

    DECLARE cur_productos CURSOR FOR
        SELECT producto_id, cantidad
        FROM detalle_pedido
        WHERE pedido_id = p_pedido_id;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    IF (SELECT estado FROM pedidos WHERE id = p_pedido_id) = 'EN_PREPARACION' THEN

        OPEN cur_productos;

        productos_loop: LOOP
            FETCH cur_productos INTO v_producto_id, v_cantidad_producto;

            IF done THEN
                LEAVE productos_loop;
            END IF;

            BEGIN
                DECLARE done2 INT DEFAULT FALSE;

                DECLARE cur_ingredientes CURSOR FOR
                    SELECT ingrediente_id, cantidad
                    FROM producto_ingrediente
                    WHERE producto_id = v_producto_id;

                DECLARE CONTINUE HANDLER FOR NOT FOUND SET done2 = TRUE;

                OPEN cur_ingredientes;

                ingredientes_loop: LOOP
                    FETCH cur_ingredientes INTO v_ingrediente_id, v_cantidad_necesaria;

                    IF done2 THEN
                        LEAVE ingredientes_loop;
                    END IF;

                    SET v_cantidad_necesaria = v_cantidad_necesaria * v_cantidad_producto;

                    -- Devolver inventario
                    UPDATE inventario
                    SET stock_actual = stock_actual + v_cantidad_necesaria
                    WHERE ingrediente_id = v_ingrediente_id;

                    INSERT INTO movimientos_inventario
                    (inventario_id, tipo, cantidad, stock_anterior, stock_nuevo, referencia, motivo)
                    SELECT 
                        id,
                        'ENTRADA',
                        v_cantidad_necesaria,
                        stock_actual - v_cantidad_necesaria,
                        stock_actual,
                        CONCAT('CANCEL-', p_pedido_id),
                        'Cancelación de pedido'
                    FROM inventario
                    WHERE ingrediente_id = v_ingrediente_id;

                END LOOP;

                CLOSE cur_ingredientes;
            END;

        END LOOP;

        CLOSE cur_productos;

    END IF;

    UPDATE pedidos
    SET estado = 'CANCELADO'
    WHERE id = p_pedido_id;

END$$

DELIMITER ;