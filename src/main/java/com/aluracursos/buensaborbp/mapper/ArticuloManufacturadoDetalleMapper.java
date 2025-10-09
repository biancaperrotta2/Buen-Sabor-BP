package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoDetalleRequest;
import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoDetalleResponse;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturadoDetalle;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ArticuloInsumoMapper.class})
public interface ArticuloManufacturadoDetalleMapper {
    ArticuloManufacturadoDetalleResponse toResponse(ArticuloManufacturadoDetalle entity);

    ArticuloManufacturadoDetalle toEntity(ArticuloManufacturadoDetalleRequest request);
}
