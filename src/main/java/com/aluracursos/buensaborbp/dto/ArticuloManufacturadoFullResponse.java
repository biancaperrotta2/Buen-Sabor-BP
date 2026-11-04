package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;
import java.util.List;

public record ArticuloManufacturadoFullResponse(
        Long id,
        String denominacion,
        String descripcion,
        Integer tiempoEstimado,
        Boolean esSemielaborado,
        Double stockActual,
        Double stockMinimo,
        Double stockMaximo,
        BigDecimal precioVenta,
        BigDecimal precioCosto, // calculado en el back
        BigDecimal ganancia,    // también calculado en el back
        String preparacion,
        CategoriaFullResponse categoria,
        List<ImagenArticuloResponse> imagenes,
        List<ArticuloManufacturadoDetalleResponse> detalles,
        // Para semielaborados (detalle y formulario): su unidad (Kilos/Litros/Unidad) y si está activo.
        String unidadMedida,
        String unidadAbreviatura,
        Boolean activo
) {
}

