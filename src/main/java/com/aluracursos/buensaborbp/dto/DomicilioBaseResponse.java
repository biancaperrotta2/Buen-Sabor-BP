package com.aluracursos.buensaborbp.dto;

public record DomicilioBaseResponse(
    Long id,
    String alias,
    String calle,
    String numero,
    String ciudad
) {}
