package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.*;
import com.aluracursos.buensaborbp.entity.Promocion;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ImagenPromocionMapper.class, ArticuloManufacturadoMapper.class})
public interface PromocionMapper {
    PromocionBaseResponse toBaseResponse(Promocion entity);

    PromocionFullResponse toFullResponse(Promocion entity);

    Promocion toEntity(PromocionRequest request);
}
