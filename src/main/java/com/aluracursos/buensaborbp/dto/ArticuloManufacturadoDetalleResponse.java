package com.aluracursos.buensaborbp.dto;

// No expone la entidad completa del componente (podía ser un insumo o, ahora, otro
// ArticuloManufacturado semielaborado): alcanza con lo que necesita mostrar la receta.
public record ArticuloManufacturadoDetalleResponse(
    Double cantidad,
    Long idComponente,
    String nombreComponente,
    Boolean esSemielaborado,
    // Para mostrar el costo de la receta: unidad en que está escrita la cantidad y costo del
    // componente por unidad base (Kilo/Litro/Unidad), igual que lo usa calcularPrecioCosto.
    String unidadComponente,
    java.math.BigDecimal costoUnitarioComponente
) {}
