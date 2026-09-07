package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.CarritoResponse;
import com.aluracursos.buensaborbp.dto.DetalleCarritoResponse;
import com.aluracursos.buensaborbp.entity.Carrito;
import com.aluracursos.buensaborbp.entity.DetalleCarrito;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CarritoMapper {

    @Mapping(target = "idArticulo", source = "articulo.id")
    @Mapping(target = "nombreArticulo", source = "articulo.nombre")
    @Mapping(target = "imagenUrl", expression = "java(detalle.getArticulo().getImagenes().isEmpty() ? null : detalle.getArticulo().getImagenes().get(0).getUrlImagen())")
    @Mapping(target = "precioUnitario", source = "articulo.precioVenta")
    @Mapping(target = "subtotal", expression = "java(detalle.getSubtotal())")
    DetalleCarritoResponse toDetalleResponse(DetalleCarrito detalle);

    List<DetalleCarritoResponse> toDetalleResponseList(List<DetalleCarrito> detalles);

    @Mapping(target = "items", source = "items")
    @Mapping(target = "subtotal", expression = "java(carrito.calcularSubtotal())")
    CarritoResponse toResponse(Carrito carrito);
}
