package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum;
import jakarta.validation.constraints.NotBlank;
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

    // Ignorado para un semielaborado: su stock es la suma de sus lotes (ExistenciaSemielaborado).
    @PositiveOrZero(message = "El stock actual no puede ser negativo")
    Double stockActual,

    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    Double stockMinimo,

    @PositiveOrZero(message = "El stock máximo no puede ser negativo")
    Double stockMaximo,

    @NotBlank(message = "La descripción es obligatoria")
    String descripcion,

    // Obligatorio (> 0) solo para un plato final; un semielaborado no se vende (ver el service).
    BigDecimal precioVenta,

    Integer tiempoEstimado, // calculado en back pero se muestra

    // Obligatoria solo para un plato final.
    String preparacion,

    CategoriaRequest categoria,
    List<ImagenArticuloRequest> imagenes,
    List<ArticuloManufacturadoDetalleRequest> articuloManufacturadoDetalles
) { }
