package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;

public record DomicilioBaseResponse(
    Long id,
    String alias,
    String calle,
    String numero,
    String ciudad,
    BigDecimal costoEnvio
) {}
