package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;

// Lista resumida: la usan la tabla de admin "Productos" (columna Tiempo) y el menú del cliente
// (agrupa por categoría), así que ambos campos tienen que venir acá, no solo en el Full.
public record ArticuloManufacturadoBaseResponse(
    Long id,
    String denominacion,
    String descripcion,
    BigDecimal precioVenta,
    String imagenUrl,
    Boolean esSemielaborado,
    Integer tiempoEstimado,
    CategoriaBaseResponse categoria
) { }
