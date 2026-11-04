package com.aluracursos.buensaborbp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PromocionDetalleRequest(
    @NotNull(message = "El artículo es obligatorio")
    Long idArticulo,

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    Integer cantidad
) {}
