package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Enums.Rol;

public record EmpleadoBaseResponse(
    String nombre,
    String apellido,
    String email,
    String telefono,
    Rol cargo
) { }
