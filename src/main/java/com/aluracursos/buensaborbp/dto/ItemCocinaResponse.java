package com.aluracursos.buensaborbp.dto;

import java.util.List;

// Línea de un pedido tal como la ve el cocinero. Si el artículo es una promoción,
// "incluye" trae lo que hay que preparar (cantidades ya multiplicadas por las del pedido);
// "preparacion" solo viene en manufacturados que la tengan cargada.
public record ItemCocinaResponse(
    String nombre,
    Integer cantidad,
    String preparacion,
    List<ItemCocinaResponse> incluye
) {}
