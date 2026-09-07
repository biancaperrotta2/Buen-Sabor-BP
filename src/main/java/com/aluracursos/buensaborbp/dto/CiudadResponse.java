package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;

public record CiudadResponse(
    Long id,
    String nombre,
    Long idProvincia,
    BigDecimal costoEnvio
    ) {}