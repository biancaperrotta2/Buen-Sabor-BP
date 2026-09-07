package com.aluracursos.buensaborbp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AgregarItemCarritoRequest(
    @NotNull(message = "El artículo es obligatorio")
    Long idArticulo,

    @Positive(message = "La cantidad debe ser mayor a 0")
    int cantidad
) {}
