package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;

public record ArticuloInsumoBaseResponse(
    Long id,
    String denominacion,
    BigDecimal precioCompra,
    CategoriaBaseResponse categoria,
    Boolean activo,
    UnidadMedidaResponse unidadMedida
) {}
