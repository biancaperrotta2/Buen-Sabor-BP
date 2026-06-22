package com.aluracursos.buensaborbp.dto;

public record ArticuloInsumoFullResponse(
    Long id,
    String denominacion,
    Double precioCompra,
    Double precioVenta,
    Double stockActual,
    Double stockMaximo,
    Boolean esParaElaborar,
    Double stockMinimo,
    Boolean activo,
    CategoriaFullResponse categoria,
    UnidadMedidaResponse unidadMedida
) {}
