package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.*;
import com.aluracursos.buensaborbp.entity.Categoria;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoriaMapper.class, CategoriaPadreMapper.class})
public interface CategoriaMapper {
    CategoriaBaseResponse toBaseResponse(Categoria entity);

    CategoriaFullResponse toFullResponse(Categoria entity);

    Categoria toEntity(CategoriaRequest request);

    List<CategoriaBaseResponse> toBaseResponseList(List<Categoria> entities);
}
