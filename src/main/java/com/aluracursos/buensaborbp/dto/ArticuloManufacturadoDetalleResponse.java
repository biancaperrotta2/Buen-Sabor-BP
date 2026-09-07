package com.aluracursos.buensaborbp.dto;

// No expone la entidad completa del componente (podía ser un insumo o, ahora, otro
// ArticuloManufacturado semielaborado): alcanza con lo que necesita mostrar la receta.
public record ArticuloManufacturadoDetalleResponse(
    Double cantidad,
    Long idComponente,
    String nombreComponente,
    Boolean esSemielaborado
) {}
