package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;

public record ResumenIngresosResponse(
    long cantidadOrdenes,
    BigDecimal totalIngresos
) {}
