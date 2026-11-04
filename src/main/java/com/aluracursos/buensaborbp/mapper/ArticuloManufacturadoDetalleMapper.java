package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoDetalleResponse;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturadoDetalle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArticuloManufacturadoDetalleMapper {

    @Mapping(target = "idComponente", source = "componente.id")
    @Mapping(target = "nombreComponente", source = "componente.nombre")
    @Mapping(target = "esSemielaborado", expression = "java(entity.getComponente() instanceof com.aluracursos.buensaborbp.entity.ArticuloManufacturado m && Boolean.TRUE.equals(m.getEsSemielaborado()))")
    @Mapping(target = "unidadComponente", expression = "java(entity.getComponente().getUnidadMedidaEnum() == null ? null : entity.getComponente().getUnidadMedidaEnum().getAbreviatura())")
    @Mapping(target = "costoUnitarioComponente", source = "componente.precioCosto")
    ArticuloManufacturadoDetalleResponse toResponse(ArticuloManufacturadoDetalle entity);
    // La creación de detalles se arma a mano en el service: requiere resolver el
    // componente por ID contra el repositorio y validar su tipo, algo que MapStruct no puede hacer.
}
