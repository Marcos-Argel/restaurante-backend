package com.restaurante.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

public class FacturaDTO {

    @Data
    public static class CrearFacturaRequest {
        @NotNull
        private Long pedidoId;
        private String clienteNombre;
        private String clienteRucNit;
        @NotBlank
        private String metodoPago;
        private BigDecimal descuento = BigDecimal.ZERO;
        private BigDecimal propina = BigDecimal.ZERO;
        private BigDecimal impuestoPorcentaje = BigDecimal.ZERO;
        @NotNull
        private BigDecimal montoPagado;
        private Long turnoId;
    }

    @Data
    public static class ItemResponse {
        private String producto;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
    }

    @Data
    public static class Response {
        private Long id;
        private String numeroFactura;
        private String pedidoNumero;
        private String clienteNombre;
        private BigDecimal subtotal;
        private BigDecimal impuesto;
        private BigDecimal descuento;
        private BigDecimal propina;
        private BigDecimal montoTotal;
        private String metodoPago;
        private BigDecimal montoPagado;
        private BigDecimal cambio;
        private String estado;
        private String fecha;
        private String cajero;
        private List<ItemResponse> items;
    }
}
