package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;
import java.util.List;

public record CarritoResponse(
    List<DetalleCarritoResponse> items,
    BigDecimal subtotal
) {}
