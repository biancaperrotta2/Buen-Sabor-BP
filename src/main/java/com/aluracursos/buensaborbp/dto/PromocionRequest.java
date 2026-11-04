package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import com.aluracursos.buensaborbp.entity.Enums.TipoPromocion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PromocionRequest(
    @NotBlank(message = "El nombre es obligatorio")
    String nombre,

    @NotBlank(message = "La descripción del descuento es obligatoria")
    String descripcionDescuento,

    @NotNull(message = "La fecha desde es obligatoria")
    LocalDate fechaDesde,

    @NotNull(message = "La fecha hasta es obligatoria")
    LocalDate fechaHasta,

    @NotNull(message = "La hora desde es obligatoria")
    LocalTime horaDesde,

    @NotNull(message = "La hora hasta es obligatoria")
    LocalTime horaHasta,

    @NotNull(message = "El precio promocional es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio promocional debe ser mayor a 0")
    BigDecimal precioVenta,

    @NotNull(message = "El tipo de promoción es obligatorio")
    TipoPromocion tipoPromocion,

    @NotNull(message = "La categoría es obligatoria")
    Long idCategoria,

    @NotEmpty(message = "La promoción debe tener al menos un artículo")
    List<@Valid PromocionDetalleRequest> detalles,

    List<String> imagenesUrls
) {}
