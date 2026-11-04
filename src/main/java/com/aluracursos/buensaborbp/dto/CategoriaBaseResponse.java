package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Enums.TipoCategoria;

public record CategoriaBaseResponse(
        Long id,
        String denominacion,
        CategoriaPadreDto categoriaPadre,
        TipoCategoria tipo
) {}
