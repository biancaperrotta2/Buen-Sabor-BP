package com.aluracursos.buensaborbp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

// Alta o corrección de un lote de semielaborado. fechaElaboracion es opcional (por defecto hoy) y
// se ignora al corregir; cantidad en la unidad del semielaborado.
public record ExistenciaSemielaboradoRequest(
        LocalDate fechaElaboracion,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a 0")
        Double cantidad,

        @NotNull(message = "La fecha de vencimiento es obligatoria")
        LocalDate fechaVencimiento
) {}
