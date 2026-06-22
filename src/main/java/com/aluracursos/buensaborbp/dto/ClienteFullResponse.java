package com.aluracursos.buensaborbp.dto;

import java.time.LocalDate;

public record ClienteFullResponse(
    Long id,
    String nombre,
    String apellido,
    String email,
    String telefono,
    ImagenUsuarioResponse fotoPerfilCliente,
    LocalDate fechaNacimiento
) {}
