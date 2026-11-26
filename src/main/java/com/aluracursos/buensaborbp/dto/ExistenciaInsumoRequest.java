package com.aluracursos.buensaborbp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

// Alta o corrección de una compra de insumo (figura 67). fechaCompra es opcional (por defecto
// hoy); cantidad en la unidad que el insumo tiene elegida (ej. gr) y costoUnitario por unidad
// base (ej. $ por kg), igual que el precio de costo del insumo.
public record ExistenciaInsumoRequest(
        LocalDate fechaCompra,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a 0")
        Double cantidad,

        @NotNull(message = "El costo unitario es obligatorio")
        @Positive(message = "El costo unitario debe ser mayor a 0")
        BigDecimal costoUnitario,

        @NotNull(message = "La fecha de vencimiento es obligatoria")
        LocalDate fechaVencimiento
) {}
