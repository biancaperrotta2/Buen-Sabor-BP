package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Enums.Rol;

public record EmpleadoFullResponse(
    Long id,
    String nombre,
    String apellido,
    String email,
    String telefono,
    Rol rol,
    ImagenUsuarioResponse fotoPerfilEmpleado
) { }
