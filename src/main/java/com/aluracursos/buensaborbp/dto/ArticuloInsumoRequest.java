package com.aluracursos.buensaborbp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record ArticuloInsumoRequest(
    @NotBlank(message = "La denominación es obligatoria")
    String denominacion,

    @NotNull(message = "El precio de compra es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio de compra no puede ser negativo")
    BigDecimal precioCompra,

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio de venta debe ser mayor a 0")
    BigDecimal precioVenta,

    Float margen,

    // Se interpreta en la unidad de este mismo request y se guarda en la unidad base
    // (ver ArticuloInsumoServiceImpl.crearArticuloInsumo/update).
    @NotNull(message = "El stock actual es obligatorio")
    @PositiveOrZero(message = "El stock actual no puede ser negativo")
    Double stockActual,

    @NotNull(message = "El stock mínimo es obligatorio")
    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    Double stockMinimo,

    @NotNull(message = "El stock máximo es obligatorio")
    @PositiveOrZero(message = "El stock máximo no puede ser negativo")
    Double stockMaximo,

    @NotNull(message = "Debe indicar si el insumo es para elaborar")
    Boolean esParaElaborar,

    List<ImagenArticuloRequest> imagenes,
    UnidadMedidaRequest unidadMedida,
    CategoriaRequest categoria
) {}
