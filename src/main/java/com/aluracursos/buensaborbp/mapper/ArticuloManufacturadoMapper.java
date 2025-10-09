package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.*;
import com.aluracursos.buensaborbp.entity.ArticuloInsumo;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturado;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoriaMapper.class, ImagenArticuloMapper.class, ArticuloManufacturadoDetalleMapper.class})
public interface ArticuloManufacturadoMapper {
    ArticuloManufacturadoBaseResponse toBaseResponse(ArticuloInsumo entity);

    ArticuloManufacturadoFullResponse toFullResponse(ArticuloInsumo entity);

    ArticuloManufacturado toEntity(ArticuloManufacturadoRequest request);

    List<ArticuloManufacturadoBaseResponse> toBaseResponseList(List<ArticuloManufacturado> entities);

    List<ArticuloManufacturadoFullResponse> toFullResponseList(List<ArticuloManufacturado> entities);
}
