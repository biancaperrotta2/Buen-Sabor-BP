package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;
import java.util.List;

public record ArticuloManufacturadoRequest(
    String denominacion,
    Integer stockMinimo,
    Integer stockMaximo,
    String descripcion,
    BigDecimal precioVenta,
    Integer tiempoEstimado, // calculado en back pero se muestra
    String preparacion, // dejar?
    CategoriaRequest categoria,
    List<ImagenArticuloRequest> imagenes,
    List<ArticuloManufacturadoDetalleRequest> articuloManufacturadoDetalles
) { }
