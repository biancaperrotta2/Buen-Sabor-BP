package com.aluracursos.buensaborbp.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.aluracursos.buensaborbp.dto.ArticuloInsumoBaseResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoFullResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoRequest;
import com.aluracursos.buensaborbp.dto.InsumoMenuResponse;
import com.aluracursos.buensaborbp.dto.UnidadMedidaRequest;
import com.aluracursos.buensaborbp.dto.UnidadMedidaResponse;
import com.aluracursos.buensaborbp.entity.ArticuloInsumo;
import com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum;

@Mapper(componentModel = "spring", uses = {CategoriaMapper.class, UnidadMedidaMapper.class})
public interface ArticuloInsumoMapper {

    // stockActual/Minimo/Maximo se guardan siempre en la unidad base (KG/L/UNIDAD, ver
    // Articulo.convertirACantidadBase) pero se muestran en la unidad que el insumo tiene
    // elegida (ej. Gramos), para que lo que el admin ve/edita coincida con esa unidad.
    @Mapping(target = "denominacion", source = "nombre")
    @Mapping(target = "precioCompra", source = "precioCosto")
    @Mapping(target = "unidadMedida", source = "unidadMedidaEnum")
    @Mapping(target = "esParaElaborar", expression = "java(entity.getEsVendible() == null ? null : !entity.getEsVendible())")
    @Mapping(target = "stockActual", expression = "java(entity.getStockActual() == null ? null : entity.convertirDesdeCantidadBase(entity.getStockActual()))")
    @Mapping(target = "stockMinimo", expression = "java(entity.getStockMinimo() == null ? null : entity.convertirDesdeCantidadBase(entity.getStockMinimo()))")
    @Mapping(target = "stockMaximo", expression = "java(entity.getStockMaximo() == null ? null : entity.convertirDesdeCantidadBase(entity.getStockMaximo()))")
    @Mapping(target = "imagenUrl", expression = "java(entity.getImagenes() == null || entity.getImagenes().isEmpty() ? null : entity.getImagenes().get(0).getUrlImagen())")
    ArticuloInsumoBaseResponse toBaseResponse(ArticuloInsumo entity);

    @Mapping(target = "denominacion", source = "nombre")
    @Mapping(target = "precioCompra", source = "precioCosto")
    @Mapping(target = "unidadMedida", source = "unidadMedidaEnum")
    @Mapping(target = "esParaElaborar", expression = "java(entity.getEsVendible() == null ? null : !entity.getEsVendible())")
    @Mapping(target = "stockActual", expression = "java(entity.getStockActual() == null ? null : entity.convertirDesdeCantidadBase(entity.getStockActual()))")
    @Mapping(target = "stockMinimo", expression = "java(entity.getStockMinimo() == null ? null : entity.convertirDesdeCantidadBase(entity.getStockMinimo()))")
    @Mapping(target = "stockMaximo", expression = "java(entity.getStockMaximo() == null ? null : entity.convertirDesdeCantidadBase(entity.getStockMaximo()))")
    @Mapping(target = "imagenUrl", expression = "java(entity.getImagenes() == null || entity.getImagenes().isEmpty() ? null : entity.getImagenes().get(0).getUrlImagen())")
    ArticuloInsumoFullResponse toFullResponse(ArticuloInsumo entity);

    // Vista pública para el menú del cliente: solo nombre, precio, foto y categoría.
    @Mapping(target = "denominacion", source = "nombre")
    @Mapping(target = "imagenUrl", expression = "java(entity.getImagenes() == null || entity.getImagenes().isEmpty() ? null : entity.getImagenes().get(0).getUrlImagen())")
    @Mapping(target = "disponible", expression = "java(entity.hayStockPara(1))")
    InsumoMenuResponse toMenuResponse(ArticuloInsumo entity);

    @Mapping(target = "nombre", source = "denominacion")
    @Mapping(target = "precioCosto", source = "precioCompra")
    @Mapping(target = "unidadMedidaEnum", source = "unidadMedida")
    @Mapping(target = "esVendible", expression = "java(!Boolean.TRUE.equals(request.esParaElaborar()))")
    @Mapping(target = "imagenes", ignore = true)
    ArticuloInsumo toEntity(ArticuloInsumoRequest request);

    default UnidadMedidaEnum toUnidadEnum(UnidadMedidaRequest request) {
        return request == null ? null : UnidadMedidaEnum.desdeCatalogo(request.denominacion(), request.abreviatura());
    }

    default UnidadMedidaResponse toUnidadResponse(UnidadMedidaEnum unidad) {
        return unidad == null ? null : new UnidadMedidaResponse(null, unidad.getDenominacion(), unidad.getAbreviatura());
    }

    List<ArticuloInsumoBaseResponse> toBaseResponseList(List<ArticuloInsumo> entities);

    List<ArticuloInsumoFullResponse> toFullResponseList(List<ArticuloInsumo> entities);
}
