package com.aluracursos.buensaborbp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacturaFullResponse {
    private Long id;
    private String numeroFactura;
    private LocalDateTime fecha;

    private ClienteBaseResponse cliente; // datos basicos del cliente
    private EmpresaResponse empresa;        // datos del restaurante
    private List<DetalleFacturaResponse> detallesFactura;

    private BigDecimal subtotal;
    private BigDecimal envio;
    private BigDecimal descuento;
    private BigDecimal total;
    private String metodoPago;

}

