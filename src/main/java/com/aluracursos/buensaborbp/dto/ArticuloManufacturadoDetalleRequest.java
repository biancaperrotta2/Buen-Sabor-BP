package com.aluracursos.buensaborbp.dto;

public record ArticuloManufacturadoDetalleRequest(
    Integer cantidad,
    ArticuloInsumoBaseResponse articuloInsumo
) {}
