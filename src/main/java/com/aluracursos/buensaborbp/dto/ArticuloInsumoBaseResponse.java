package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;

public record ArticuloInsumoBaseResponse(
    Long id,
    String denominacion,
    BigDecimal precioCompra,
    BigDecimal precioVenta,
    Float margen,
    Double stockActual,
    Double stockMinimo,
    Double stockMaximo,
    Boolean esParaElaborar,
    CategoriaBaseResponse categoria,
    Boolean activo,
    UnidadMedidaResponse unidadMedida
) {}
