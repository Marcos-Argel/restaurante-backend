package com.restaurante.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

public class PedidoDTO {

    @Data
    public static class CrearPedidoRequest {
        private Integer mesaId;
        @NotBlank
        private String tipoPedido;
        private String clienteNombre;
        private String clienteTelefono;
        private String clienteDireccion;
        private String notasEspeciales;
        @NotEmpty
        private List<ItemPedido> items;
    }

    @Data
    public static class ItemPedido {
        @NotNull
        private Long productoId;
        @NotNull @Min(1)
        private Integer cantidad;
        private String notas;
    }

    @Data
    public static class Response {
        private Long id;
        private String numeroPedido;
        private String mesa;
        private String mesero;
        private String tipoPedido;
        private String estado;
        private BigDecimal subtotal;
        private BigDecimal total;
        private String fechaCreacion;
        private String notasEspeciales;
        private List<DetalleResponse> items;
    }

    @Data
    public static class DetalleResponse {
        private Long id;
        private String producto;
        private String categoria;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
        private String notas;
        private String estado;
    }

    @Data
    public static class CambiarEstadoRequest {
        @NotBlank
        private String estado;
    }
}
