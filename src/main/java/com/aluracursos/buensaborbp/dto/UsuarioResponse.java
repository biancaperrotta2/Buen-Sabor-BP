package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Enums.Rol;

public record UsuarioResponse(
    Long id,
    String email,
    Rol rol
) {}
