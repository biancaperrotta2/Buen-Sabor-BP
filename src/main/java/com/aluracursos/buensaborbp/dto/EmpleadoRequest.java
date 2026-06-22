package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Enums.Rol;

public record EmpleadoRequest(
    String nombre,
    String apellido,
    String telefono,
    String email,
    String password,
    Rol rol
) {}
