package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.ImagenPromocionRequest;
import com.aluracursos.buensaborbp.dto.ImagenPromocionResponse;
import com.aluracursos.buensaborbp.entity.ImagenPromocion;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ImagenPromocionMapper {
    ImagenPromocionResponse toResponse(ImagenPromocion entity);

    ImagenPromocion toEntity(ImagenPromocionRequest request);
}