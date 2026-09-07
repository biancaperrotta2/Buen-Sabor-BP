package com.aluracursos.buensaborbp.dto;

// idComponente puede ser un insumo o un ArticuloManufacturado marcado esSemielaborado=true
// (se valida en el service; nunca un plato final ni una promoción).
public record ArticuloManufacturadoDetalleRequest(
    Long idComponente,
    Double cantidad
) {}
