package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record ArticuloManufacturadoRequest(
    @NotBlank(message = "La denominación es obligatoria")
    String denominacion,

    // Un semielaborado (ej. masa de pizza) tiene stock propio, que se prepara en lote y se
    // consume de a poco; un plato final no lleva stock propio (cascada a sus componentes),
    // así que estos cuatro solo son relevantes cuando esSemielaborado=true (ver el service).
    // Un plato final siempre se vende por UNIDAD, así que unidadMedida se ignora si esto es false.
    Boolean esSemielaborado,

    UnidadMedidaEnum unidadMedida,

    @PositiveOrZero(message = "El stock actual no puede ser negativo")
    Double stockActual,

    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    Double stockMinimo,

    @PositiveOrZero(message = "El stock máximo no puede ser negativo")
    Double stockMaximo,

    @NotBlank(message = "La descripción es obligatoria")
    String descripcion,

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio de venta debe ser mayor a 0")
    BigDecimal precioVenta,

    Integer tiempoEstimado, // calculado en back pero se muestra

    @NotBlank(message = "La preparación es obligatoria")
    String preparacion, // dejar?

    CategoriaRequest categoria,
    List<ImagenArticuloRequest> imagenes,
    List<ArticuloManufacturadoDetalleRequest> articuloManufacturadoDetalles
) { }
