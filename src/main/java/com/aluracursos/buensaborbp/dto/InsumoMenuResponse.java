package com.aluracursos.buensaborbp.dto;

import java.math.BigDecimal;

// Insumo que se vende directo (ej. una gaseosa) tal como lo ve el cliente en el menú. Es
// público, así que a propósito no expone costo, margen ni stock.
public record InsumoMenuResponse(
        Long id,
        String denominacion,
        BigDecimal precioVenta,
        String imagenUrl,
        CategoriaBaseResponse categoria
) {}
