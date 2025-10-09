package com.aluracursos.buensaborbp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaPadreDto {
    private Long id;
    private String denominacion;
    private List<CategoriaBaseResponse> subcategorias;
}