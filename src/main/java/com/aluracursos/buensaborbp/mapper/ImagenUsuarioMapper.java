package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.ImagenUsuarioRequest;
import com.aluracursos.buensaborbp.dto.ImagenUsuarioResponse;
import com.aluracursos.buensaborbp.entity.ImagenUsuario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ImagenUsuarioMapper {
    ImagenUsuarioResponse toResponse(ImagenUsuario entity);

    ImagenUsuario toEntity(ImagenUsuarioRequest request);
}
