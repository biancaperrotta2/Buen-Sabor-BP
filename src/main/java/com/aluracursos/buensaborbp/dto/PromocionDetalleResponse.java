package com.aluracursos.buensaborbp.dto;

public record PromocionDetalleResponse(
    Long idArticulo,
    String nombreArticulo,
    Integer cantidad
) {}