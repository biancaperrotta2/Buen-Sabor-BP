package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.ImagenUsuarioResponse;
import com.aluracursos.buensaborbp.entity.ImagenUsuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ImagenUsuarioMapper {
    @Mapping(target = "id_imagenUsuario", source = "id")
    @Mapping(target = "url", source = "urlImagen")
    ImagenUsuarioResponse toResponse(ImagenUsuario imagen);
}
