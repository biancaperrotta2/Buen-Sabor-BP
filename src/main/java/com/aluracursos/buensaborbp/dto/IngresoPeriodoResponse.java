package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;

// "periodo" es "yyyy-MM-dd" para agrupación DIARIO o "yyyy-MM" para MENSUAL.
public record IngresoPeriodoResponse(
    String periodo,
    BigDecimal total
) {}
