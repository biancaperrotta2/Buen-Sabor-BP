package com.aluracursos.buensaborbp.dto;

import java.util.List;

// Dónde se usa un insumo: lo consulta el formulario de Insumos antes de cambiarle la unidad,
// para mostrar qué recetas se van a ajustar (o qué promociones lo impiden).
public record UsoInsumoResponse(
        List<RecetaQueLoUsa> recetas,
        List<String> promociones
) {
    // cantidad: tal como está guardada, en la unidad actual del insumo.
    public record RecetaQueLoUsa(Long idProducto, String producto, Double cantidad) {}
}
