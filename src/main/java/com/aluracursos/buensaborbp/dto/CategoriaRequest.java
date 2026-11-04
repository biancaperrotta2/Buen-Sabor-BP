package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Enums.TipoCategoria;
import jakarta.validation.constraints.NotBlank;

public record CategoriaRequest(
    @NotBlank(message = "La denominación es obligatoria")
    String denominacion,

    Long idCategoriaPadre,

    // Opcional: null = PRODUCTO. Una subcategoría siempre hereda el tipo de su padre.
    TipoCategoria tipo
) {
    // Los requests de Insumo/Producto anidan una categoría solo para referenciarla por id.
    public CategoriaRequest(String denominacion, Long idCategoriaPadre) {
        this(denominacion, idCategoriaPadre, null);
    }
}
