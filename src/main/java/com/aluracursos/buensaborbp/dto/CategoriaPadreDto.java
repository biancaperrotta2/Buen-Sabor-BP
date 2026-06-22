package com.aluracursos.buensaborbp.dto;

import java.util.List;

public record CategoriaPadreDto(
    Long id,
    String denominacion,
    List<CategoriaBaseResponse> subcategorias
) {}