package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// Una fila de la tabla "Existencias" del detalle de insumo (figura 66). cantidad en la unidad
// que el insumo tiene elegida; costoUnitario por unidad base; vencida = ya pasó el vencimiento.
public record ExistenciaInsumoResponse(
        Long id,
        LocalDate fechaCompra,
        Double cantidad,
        BigDecimal costoUnitario,
        BigDecimal costoTotal,
        LocalDate fechaVencimiento,
        boolean vencida
) {}
