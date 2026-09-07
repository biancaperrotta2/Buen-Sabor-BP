package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.ImagenPromocionResponse;
import com.aluracursos.buensaborbp.entity.ImagenPromocion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ImagenPromocionMapper {
    @Mapping(target = "id_imagenPromocion", source = "id")
    @Mapping(target = "url", source = "urlImagen")
    ImagenPromocionResponse toResponse(ImagenPromocion imagen);
    List<ImagenPromocionResponse> toResponseList(List<ImagenPromocion> imagenes);
}
