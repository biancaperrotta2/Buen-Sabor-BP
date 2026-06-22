package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.ArticuloInsumo;

public record ArticuloManufacturadoDetalleResponse(
    Integer cantidad,
    ArticuloInsumo articuloInsumo
) {}
