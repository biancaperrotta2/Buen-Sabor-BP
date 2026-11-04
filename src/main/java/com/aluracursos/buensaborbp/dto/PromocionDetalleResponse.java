package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;

public record PromocionDetalleResponse(
    Long idArticulo,
    String nombreArticulo,
    Integer cantidad,
    BigDecimal precioUnitario
) {}