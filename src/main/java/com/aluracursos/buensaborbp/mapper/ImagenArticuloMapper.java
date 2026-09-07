package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.ImagenArticuloResponse;
import com.aluracursos.buensaborbp.entity.ImagenArticulo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ImagenArticuloMapper {
    @Mapping(target = "id_imagenArticulo", source = "id")
    @Mapping(target = "url", source = "urlImagen")
    ImagenArticuloResponse toResponse(ImagenArticulo entity);
}
