package com.aluracursos.buensaborbp.dto;

public record DomicilioFullResponse(
    String calle,
    String numero,
    String piso,
    String departamento,
    String ciudad,
    String aclaraciones,
    String alias
) {}
