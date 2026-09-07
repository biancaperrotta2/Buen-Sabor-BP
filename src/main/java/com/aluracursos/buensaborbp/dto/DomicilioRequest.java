package com.aluracursos.buensaborbp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DomicilioRequest(
    @NotBlank(message = "La calle es obligatoria")
    String calle,

    @NotBlank(message = "El número es obligatorio")
    String numero,

    String cp,
    String piso,
    String nroDepto,

    @NotNull(message = "La ciudad es obligatoria")
    Long idCiudad,

    String aclaraciones,
    String alias
) {}
