package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.*;
import com.aluracursos.buensaborbp.entity.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoriaPadreMapper.class})
public interface CategoriaMapper {
    @Named("categoriaBaseConPadre")
    CategoriaBaseResponse toBaseResponse(Categoria entity);

    @Mapping(target = "nombre", source = "denominacion")
    @Mapping(target = "categoriaPadre", qualifiedByName = "categoriaBaseConPadre")
    @Mapping(target = "subcategorias", qualifiedByName = "categoriaBaseSinPadre")
    CategoriaFullResponse toFullResponse(Categoria entity);

    // El tipo lo resuelve el service (default PRODUCTO, o heredado del padre).
    @Mapping(target = "tipo", ignore = true)
    Categoria toEntity(CategoriaRequest request);

    List<CategoriaBaseResponse> toBaseResponseList(List<Categoria> entities);
}
