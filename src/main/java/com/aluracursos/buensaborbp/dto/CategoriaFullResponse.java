package com.aluracursos.buensaborbp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaFullResponse {
    private Long id;
    private String nombre;
    private CategoriaBaseResponse categoriaPadre; // null si es categoria padre
    private List<CategoriaBaseResponse> subcategorias;
}
