package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.*;
import com.aluracursos.buensaborbp.entity.Promocion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;
import com.aluracursos.buensaborbp.dto.PromocionBaseResponse;
import com.aluracursos.buensaborbp.dto.PromocionFullResponse;
import com.aluracursos.buensaborbp.dto.PromocionDetalleResponse;
import com.aluracursos.buensaborbp.entity.PromocionDetalle;

@Mapper(componentModel = "spring")
public interface PromocionMapper {

    @Mapping(target = "urlImagen", expression = "java(promocion.getImagenesPromocion().isEmpty() ? null : promocion.getImagenesPromocion().get(0).getUrl())")
    PromocionBaseResponse toBaseResponse(Promocion promocion);
    
    List<PromocionBaseResponse> toBaseResponseList(List<Promocion> promociones);

    @Mapping(target = "detalles", source = "promocionDetalle")
    @Mapping(target = "imagenes", source = "imagenesPromocion")
    PromocionFullResponse toFullResponse(Promocion promocion);

    @Mapping(target = "idArticulo", source = "articulos.id")
    PromocionDetalleResponse toDetalleResponse(PromocionDetalle detalle);
}
@Mapper(componentModel = "spring")
public interface PromocionMapper {

    @Mapping(target = "nombreCategoria", source = "categoria.denominacion")
    @Mapping(target = "imagenes", source = "imagenesPromocion")
    @Mapping(target = "detalles", source = "promocionDetalle")
    PromocionFullResponse toFullResponse(Promocion promocion);

    // Mapeo automático para las imágenes individuales
    ImagenPromocionResponse toImagenResponse(ImagenPromocion imagen);

    // Mapeo para los detalles individuales
    @Mapping(target = "idArticulo", source = "articulos.id")
    @Mapping(target = "nombreArticulo", source = "articulos.nombre")
    PromocionDetalleResponse toDetalleResponse(PromocionDetalle detalle);
}