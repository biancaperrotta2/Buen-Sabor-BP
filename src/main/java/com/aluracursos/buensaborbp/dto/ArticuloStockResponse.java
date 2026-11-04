package com.aluracursos.buensaborbp.dto;

public record ArticuloStockResponse(
    Long id,
    String nombre,
    Double stockActual,
    Double stockMinimo,
    Double stockMaximo,
    NivelStock nivel,
    CategoriaStock categoria
) {}
