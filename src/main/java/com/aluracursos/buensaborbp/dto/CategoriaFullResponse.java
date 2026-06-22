package com.aluracursos.buensaborbp.dto;

import java.util.List;

public record CategoriaFullResponse(
    Long id,
    String nombre,
    CategoriaBaseResponse categoriaPadre, // null si es categoria padre
    List<CategoriaBaseResponse> subcategorias
) {}
