package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoBaseResponse;
import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoFullResponse;
import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoRequest;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturado;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoriaMapper.class, ImagenArticuloMapper.class, ArticuloManufacturadoDetalleMapper.class})
public interface ArticuloManufacturadoMapper {

    @Mapping(target = "denominacion", source = "nombre")
    @Mapping(target = "imagenUrl", expression = "java(entity.getImagenes().isEmpty() ? null : entity.getImagenes().get(0).getUrlImagen())")
    ArticuloManufacturadoBaseResponse toBaseResponse(ArticuloManufacturado entity);

    @Mapping(target = "denominacion", source = "nombre")
    @Mapping(target = "tiempoEstimado", source = "tiempoEstimadoMinutos")
    @Mapping(target = "detalles", source = "articuloManufacturadoDetalles")
    @Mapping(target = "ganancia", expression = "java(entity.getPrecioVenta() != null && entity.getPrecioCosto() != null ? entity.getPrecioVenta().subtract(entity.getPrecioCosto()) : null)")
    ArticuloManufacturadoFullResponse toFullResponse(ArticuloManufacturado entity);

    @Mapping(target = "nombre", source = "denominacion")
    @Mapping(target = "tiempoEstimadoMinutos", ignore = true)
    @Mapping(target = "imagenes", ignore = true)
    @Mapping(target = "articuloManufacturadoDetalles", ignore = true)
    ArticuloManufacturado toEntity(ArticuloManufacturadoRequest request);

    List<ArticuloManufacturadoBaseResponse> toBaseResponseList(List<ArticuloManufacturado> entities);

    List<ArticuloManufacturadoFullResponse> toFullResponseList(List<ArticuloManufacturado> entities);
}
