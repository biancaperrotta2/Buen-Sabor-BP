package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FacturaBaseResponse(
    Long idFactura,
    Long idCliente,
    LocalDate fecha,
    BigDecimal total,
    String numeroFactura
) {}
