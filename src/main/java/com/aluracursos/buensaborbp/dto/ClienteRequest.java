package com.aluracursos.buensaborbp.dto;

import java.time.LocalDate;

public record ClienteRequest(
    String nombre,
    String apellido,
    String telefono,
    String email,
    String password,
    LocalDate fechaNacimiento
) {}
