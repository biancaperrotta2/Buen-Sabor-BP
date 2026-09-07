package com.aluracursos.buensaborbp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CiudadRequest(
    @NotBlank(message = "El nombre es obligatorio")
    String nombre,

    @NotNull(message = "La provincia es obligatoria")
    Long idProvincia,

    @NotNull(message = "El costo de envío es obligatorio")
    @PositiveOrZero(message = "El costo de envío no puede ser negativo")
    BigDecimal costoEnvio
) {}
