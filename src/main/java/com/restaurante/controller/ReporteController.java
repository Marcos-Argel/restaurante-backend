package com.restaurante.controller;

import com.restaurante.dto.ApiResponse;
import com.restaurante.entity.Factura;
import com.restaurante.entity.Pedido;
import com.restaurante.repository.FacturaRepository;
import com.restaurante.repository.PedidoRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final FacturaRepository facturaRepository;
    private final PedidoRepository pedidoRepository;

    @GetMapping("/ventas-dia")
    public ResponseEntity<ApiResponse<ResumenVentas>> ventasDia() {
        List<Factura> facturas = facturaRepository.findFacturasHoy();
        ResumenVentas resumen = calcularResumen(facturas);
        return ResponseEntity.ok(ApiResponse.ok(resumen));
    }

    @GetMapping("/ventas-periodo")
    public ResponseEntity<ApiResponse<ResumenVentas>> ventasPeriodo(
            @RequestParam String inicio,
            @RequestParam String fin) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime desde = LocalDateTime.parse(inicio + "T00:00:00");
        LocalDateTime hasta = LocalDateTime.parse(fin + "T23:59:59");
        List<Factura> facturas = facturaRepository.findByFechaBetween(desde, hasta);
        return ResponseEntity.ok(ApiResponse.ok(calcularResumen(facturas)));
    }

    @GetMapping("/pedidos-cancelados")
    public ResponseEntity<ApiResponse<Long>> pedidosCancelados() {
        long count = pedidoRepository.findAll().stream()
                .filter(p -> p.getEstado() == Pedido.EstadoPedido.CANCELADO)
                .count();
        return ResponseEntity.ok(ApiResponse.ok(count));
    }

    @GetMapping("/metodos-pago")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> metodosPago() {
        List<Factura> facturas = facturaRepository.findFacturasHoy();
        Map<String, BigDecimal> resultado = facturas.stream()
                .filter(f -> f.getEstado() == Factura.EstadoFactura.EMITIDA)
                .collect(Collectors.groupingBy(
                        f -> f.getMetodoPago().name(),
                        Collectors.reducing(BigDecimal.ZERO, Factura::getMontoTotal, BigDecimal::add)
                ));
        return ResponseEntity.ok(ApiResponse.ok(resultado));
    }

    private ResumenVentas calcularResumen(List<Factura> facturas) {
        ResumenVentas r = new ResumenVentas();
        r.setTotalFacturas((long) facturas.size());

        facturas.stream()
                .filter(f -> f.getEstado() == Factura.EstadoFactura.EMITIDA)
                .forEach(f -> {
                    r.setSubtotal(r.getSubtotal().add(f.getSubtotal()));
                    r.setImpuestos(r.getImpuestos().add(f.getImpuesto()));
                    r.setDescuentos(r.getDescuentos().add(f.getDescuento()));
                    r.setPropinas(r.getPropinas().add(f.getPropina()));
                    r.setTotalVentas(r.getTotalVentas().add(f.getMontoTotal()));

                    switch (f.getMetodoPago()) {
                        case EFECTIVO -> r.setEfectivo(r.getEfectivo().add(f.getMontoTotal()));
                        case TARJETA_DEBITO, TARJETA_CREDITO -> r.setTarjeta(r.getTarjeta().add(f.getMontoTotal()));
                        case TRANSFERENCIA -> r.setTransferencia(r.getTransferencia().add(f.getMontoTotal()));
                        default -> {}
                    }
                });

        if (r.getTotalFacturas() > 0) {
            r.setTicketPromedio(r.getTotalVentas().divide(
                    BigDecimal.valueOf(r.getTotalFacturas()), 2, java.math.RoundingMode.HALF_UP));
        }

        return r;
    }

    @Data
    public static class ResumenVentas {
        private Long totalFacturas = 0L;
        private BigDecimal subtotal = BigDecimal.ZERO;
        private BigDecimal impuestos = BigDecimal.ZERO;
        private BigDecimal descuentos = BigDecimal.ZERO;
        private BigDecimal propinas = BigDecimal.ZERO;
        private BigDecimal totalVentas = BigDecimal.ZERO;
        private BigDecimal efectivo = BigDecimal.ZERO;
        private BigDecimal tarjeta = BigDecimal.ZERO;
        private BigDecimal transferencia = BigDecimal.ZERO;
        private BigDecimal ticketPromedio = BigDecimal.ZERO;
    }
}
