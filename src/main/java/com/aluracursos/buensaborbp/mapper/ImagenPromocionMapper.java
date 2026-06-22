package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.ImagenPromocionResponse;
import com.aluracursos.buensaborbp.entity.ImagenPromocion;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ImagenPromocionMapper {
    ImagenPromocionResponse toResponse(ImagenPromocion imagen);
    List<ImagenPromocionResponse> toResponseList(List<ImagenPromocion> imagenes);
}