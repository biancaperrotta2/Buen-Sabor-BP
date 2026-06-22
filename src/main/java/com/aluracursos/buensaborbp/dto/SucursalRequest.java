package com.aluracursos.buensaborbp.dto;

import java.time.LocalTime;

public record SucursalRequest(
    String nombre,
    LocalTime horarioApertura,
    LocalTime horarioCierre,
    Long idEmpresa,
    DomicilioRequest domicilio 
) {}