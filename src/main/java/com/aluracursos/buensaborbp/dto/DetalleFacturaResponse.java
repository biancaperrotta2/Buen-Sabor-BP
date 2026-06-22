package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;

public record DetalleFacturaResponse(
    String nombreArticulo,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal
) {}
