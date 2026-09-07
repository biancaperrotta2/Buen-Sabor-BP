package com.aluracursos.buensaborbp.dto;

import jakarta.validation.constraints.NotBlank;

public record UnidadMedidaRequest(
    @NotBlank(message = "La denominación es obligatoria")
    String denominacion, //kilogramo

    @NotBlank(message = "La abreviatura es obligatoria")
    String abreviatura //kg
) {}
