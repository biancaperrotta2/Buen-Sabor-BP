package com.aluracursos.buensaborbp.dto;

import java.time.LocalTime;

public record SucursalResponse(
    Long id,
    String nombre,
    LocalTime horarioApertura,
    LocalTime horarioCierre,
    String direccionCompleta // Ej: "Av. Arístides Villanueva 123, Mendoza"
) {}
