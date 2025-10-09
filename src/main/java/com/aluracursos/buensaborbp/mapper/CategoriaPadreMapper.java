package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.CategoriaPadreDto;
import com.aluracursos.buensaborbp.entity.Categoria;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoriaMapper.class})
public interface CategoriaPadreMapper {
    CategoriaPadreDto toResponse(Categoria entity);

    List<CategoriaPadreDto> toDTOResponseList(List<Categoria> entities); //ver si hace falta ??

}
