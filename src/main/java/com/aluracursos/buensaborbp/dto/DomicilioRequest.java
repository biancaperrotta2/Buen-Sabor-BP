package com.aluracursos.buensaborbp.dto;

public record DomicilioRequest(
    String calle,
    String numero,
    String cp,
    String piso,
    String nroDepto,
    Long idCiudad,
    String aclaraciones,
    String alias
) {}