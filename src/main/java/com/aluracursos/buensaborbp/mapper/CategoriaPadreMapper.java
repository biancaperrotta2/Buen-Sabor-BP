package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.CategoriaBaseResponse;
import com.aluracursos.buensaborbp.dto.CategoriaPadreDto;
import com.aluracursos.buensaborbp.entity.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoriaPadreMapper {
    // Las subcategorías del padre van sin su propio padre: si no, hija -> padre -> subcategorías ->
    // padre... no termina nunca (StackOverflowError en cualquier categoría con subcategorías).
    @Mapping(target = "subcategorias", qualifiedByName = "categoriaBaseSinPadre")
    CategoriaPadreDto toResponse(Categoria entity);

    List<CategoriaPadreDto> toDTOResponseList(List<Categoria> entities); //ver si hace falta ??

    // No usa CategoriaMapper a propósito: evita tanto una dependencia circular de beans
    // (CategoriaMapper ya depende de CategoriaPadreMapper) como una recursión infinita de datos
    // (categoria -> categoriaPadre -> subcategorias -> categoriaPadre -> ...).
    @Named("categoriaBaseSinPadre")
    @Mapping(target = "categoriaPadre", ignore = true)
    CategoriaBaseResponse toBaseResponseSinPadre(Categoria entity);
}
