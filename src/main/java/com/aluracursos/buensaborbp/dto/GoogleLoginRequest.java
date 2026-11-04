package com.aluracursos.buensaborbp.dto;

import jakarta.validation.constraints.NotBlank;

// idToken: el credential que entrega Google Identity Services en el frontend después de
// que el usuario elige su cuenta de Google (no es nuestro JWT, es de Google).
public record GoogleLoginRequest(
    @NotBlank(message = "El idToken es obligatorio")
    String idToken
) {}
