package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;

public record ArticuloManufacturadoBaseResponse(
    Long id,
    String denominacion,
    String descripcion,
    BigDecimal precioVenta,
    String imagenUrl
) { }

