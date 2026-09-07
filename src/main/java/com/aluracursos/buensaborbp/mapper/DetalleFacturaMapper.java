package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.DetalleFacturaResponse;
import com.aluracursos.buensaborbp.entity.DetalleFactura;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DetalleFacturaMapper {
    // Cada línea de factura es o un Articulo o una Promocion (ambos heredan nombre/precioVenta de Articulo)
    @Mapping(target = "subtotal", source = "subTotal")
    @Mapping(target = "nombreArticulo", expression = "java(entity.getArticulo() != null ? entity.getArticulo().getNombre() : (entity.getPromocion() != null ? entity.getPromocion().getNombre() : null))")
    @Mapping(target = "precioUnitario", expression = "java(entity.getArticulo() != null ? entity.getArticulo().getPrecioVenta() : (entity.getPromocion() != null ? entity.getPromocion().getPrecioVenta() : null))")
    DetalleFacturaResponse toResponse(DetalleFactura entity);
}
