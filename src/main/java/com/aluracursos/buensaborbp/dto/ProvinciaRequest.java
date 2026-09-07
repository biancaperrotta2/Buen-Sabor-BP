package com.aluracursos.buensaborbp.dto;

import jakarta.validation.constraints.NotBlank;

public record ProvinciaRequest(
    @NotBlank(message = "El nombre es obligatorio")
    String nombre
) {}
