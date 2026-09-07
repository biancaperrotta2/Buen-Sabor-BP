package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;

public record DetalleCarritoResponse(
    Long idArticulo,
    String nombreArticulo,
    String imagenUrl,
    BigDecimal precioUnitario,
    Integer cantidad,
    BigDecimal subtotal
) {}
