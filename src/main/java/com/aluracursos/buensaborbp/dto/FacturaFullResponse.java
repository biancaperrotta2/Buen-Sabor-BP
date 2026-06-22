package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FacturaFullResponse(
    Long id,
    String numeroFactura,
    LocalDateTime fecha,
    ClienteBaseResponse cliente, 
    EmpresaResponse empresa,       
    List<DetalleFacturaResponse> detallesFactura,
    BigDecimal subtotal,
    BigDecimal envio,
    BigDecimal descuento,
    BigDecimal total,
    String metodoPago
) { }

