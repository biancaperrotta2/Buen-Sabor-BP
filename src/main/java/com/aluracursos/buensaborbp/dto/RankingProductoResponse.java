package com.aluracursos.buensaborbp.dto;

public record RankingProductoResponse(
    Long idArticulo,
    String nombreArticulo,
    Long cantidadVendida
) {}
