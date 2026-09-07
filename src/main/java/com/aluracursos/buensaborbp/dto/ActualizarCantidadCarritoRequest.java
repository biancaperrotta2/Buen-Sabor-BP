package com.aluracursos.buensaborbp.dto;

import jakarta.validation.constraints.Positive;

public record ActualizarCantidadCarritoRequest(
    @Positive(message = "La cantidad debe ser mayor a 0")
    int cantidad
) {}
