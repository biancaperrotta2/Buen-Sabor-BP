package com.aluracursos.buensaborbp.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoriaRequest(
    @NotBlank(message = "La denominación es obligatoria")
    String denominacion,

    Long idCategoriaPadre
) { }
