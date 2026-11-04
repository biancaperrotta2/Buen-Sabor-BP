package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;

// Fila de la tabla "Semielaborados" (historia #21): foto, nombre, costo y disponibilidad
// (semáforo calculado con la misma regla que el gráfico de stock, ver NivelStock).
public record SemielaboradoAdminResponse(
        Long id,
        String denominacion,
        String imagenUrl,
        BigDecimal precioCosto,
        Double stockActual,
        Double stockMinimo,
        Double stockMaximo,
        String unidadAbreviatura,
        NivelStock nivelStock,
        Boolean activo,
        CategoriaBaseResponse categoria
) {}
