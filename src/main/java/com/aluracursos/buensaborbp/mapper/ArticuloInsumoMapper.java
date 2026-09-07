package com.aluracursos.buensaborbp.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.aluracursos.buensaborbp.dto.ArticuloInsumoBaseResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoFullResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoRequest;
import com.aluracursos.buensaborbp.entity.ArticuloInsumo;

@Mapper(componentModel = "spring", uses = {CategoriaMapper.class, UnidadMedidaMapper.class})
public interface ArticuloInsumoMapper {

    @Mapping(target = "denominacion", source = "nombre")
    @Mapping(target = "precioCompra", source = "precioCosto")
    @Mapping(target = "esParaElaborar", expression = "java(entity.getEsVendible() == null ? null : !entity.getEsVendible())")
    ArticuloInsumoBaseResponse toBaseResponse(ArticuloInsumo entity);

    @Mapping(target = "denominacion", source = "nombre")
    @Mapping(target = "precioCompra", source = "precioCosto")
    @Mapping(target = "esParaElaborar", expression = "java(entity.getEsVendible() == null ? null : !entity.getEsVendible())")
    ArticuloInsumoFullResponse toFullResponse(ArticuloInsumo entity);

    @Mapping(target = "nombre", source = "denominacion")
    @Mapping(target = "precioCosto", source = "precioCompra")
    @Mapping(target = "esVendible", expression = "java(!Boolean.TRUE.equals(request.esParaElaborar()))")
    @Mapping(target = "imagenes", ignore = true)
    ArticuloInsumo toEntity(ArticuloInsumoRequest request);

    List<ArticuloInsumoBaseResponse> toBaseResponseList(List<ArticuloInsumo> entities);

    List<ArticuloInsumoFullResponse> toFullResponseList(List<ArticuloInsumo> entities);
}
