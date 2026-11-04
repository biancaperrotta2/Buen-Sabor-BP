package com.aluracursos.buensaborbp.dto;

import jakarta.validation.constraints.NotBlank;

// idToken: el que entrega Firebase Authentication en el frontend luego de iniciar sesión
// (por ejemplo, después de resetear la contraseña desde la ventana de Firebase).
public record FirebaseLoginRequest(
    @NotBlank(message = "El idToken es obligatorio")
    String idToken
) {}
