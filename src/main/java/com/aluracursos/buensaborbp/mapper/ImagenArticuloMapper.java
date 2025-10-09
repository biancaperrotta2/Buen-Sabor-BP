package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.ImagenArticuloRequest;
import com.aluracursos.buensaborbp.dto.ImagenArticuloResponse;
import com.aluracursos.buensaborbp.entity.ImagenArticulo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ImagenArticuloMapper {
    ImagenArticuloResponse toResponse(ImagenArticulo entity);

    ImagenArticulo toEntity(ImagenArticuloRequest request);
}
