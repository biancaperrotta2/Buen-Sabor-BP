package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.PromocionBaseResponse;
import com.aluracursos.buensaborbp.dto.PromocionDetalleResponse;
import com.aluracursos.buensaborbp.dto.PromocionFullResponse;
import com.aluracursos.buensaborbp.entity.Promocion;
import com.aluracursos.buensaborbp.entity.PromocionDetalle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ImagenPromocionMapper.class})
public interface PromocionMapper {

    @Mapping(target = "urlImagen", expression = "java(promocion.getImagenesPromocion().isEmpty() ? null : promocion.getImagenesPromocion().get(0).getUrlImagen())")
    PromocionBaseResponse toBaseResponse(Promocion promocion);

    List<PromocionBaseResponse> toBaseResponseList(List<Promocion> promociones);

    @Mapping(target = "nombreCategoria", source = "categoria.denominacion")
    @Mapping(target = "imagenes", source = "imagenesPromocion")
    @Mapping(target = "detalles", source = "promocionDetalle")
    PromocionFullResponse toFullResponse(Promocion promocion);

    // Mapeo para los detalles individuales
    @Mapping(target = "idArticulo", source = "articulos.id")
    @Mapping(target = "nombreArticulo", source = "articulos.nombre")
    @Mapping(target = "precioUnitario", source = "articulos.precioVenta")
    PromocionDetalleResponse toDetalleResponse(PromocionDetalle detalle);
}
