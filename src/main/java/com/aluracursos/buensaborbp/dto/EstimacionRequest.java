package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Enums.TipoEnvio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// Lo que hay en el carrito (vive en el navegador hasta confirmar) para estimar la hora.
public record EstimacionRequest(
    @NotNull TipoEnvio tipoEnvio,
    @NotEmpty @Valid List<Item> items
) {
    public record Item(@NotNull Long idArticulo, @NotNull @Min(1) Integer cantidad) {}
}
