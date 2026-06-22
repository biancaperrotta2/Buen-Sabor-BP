package com.aluracursos.buensaborbp.dto;

public record CategoriaBaseResponse(
        Long id,
        String denominacion,
        CategoriaPadreDto categoriaPadre
) {}
