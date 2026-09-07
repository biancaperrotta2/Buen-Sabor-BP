package com.aluracursos.buensaborbp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ActualizarCostoEnvioRequest(
    @NotNull(message = "El costo de envío es obligatorio")
    @PositiveOrZero(message = "El costo de envío no puede ser negativo")
    BigDecimal costoEnvio
) {}
