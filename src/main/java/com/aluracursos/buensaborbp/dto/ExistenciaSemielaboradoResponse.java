package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// estado: VIGENTE (se puede usar), AGOTADO (se usó todo), VENCIDO (no se usa, se puede dar de
// baja) o DESCARTADO (vencido y dado de baja). editable = todavía no se usó en pedidos: solo así
// se puede corregir o borrar (devolviendo los insumos).
public record ExistenciaSemielaboradoResponse(
        Long id,
        LocalDate fechaElaboracion,
        LocalDate fechaVencimiento,
        Double cantidad,
        Double cantidadRestante,
        BigDecimal costoUnitario,
        BigDecimal costoTotal,
        String estado,
        boolean editable
) {}
