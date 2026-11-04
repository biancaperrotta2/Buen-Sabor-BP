package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;

public record ArticuloInsumoFullResponse(
    Long id,
    String denominacion,
    BigDecimal precioCompra,
    BigDecimal precioVenta,
    Float margen,
    Double stockActual,
    Double stockMaximo,
    Double stockMinimo,
    Double stockReservado,
    Boolean esParaElaborar,
    Boolean activo,
    CategoriaFullResponse categoria,
    UnidadMedidaResponse unidadMedida,
    String imagenUrl
) {}
