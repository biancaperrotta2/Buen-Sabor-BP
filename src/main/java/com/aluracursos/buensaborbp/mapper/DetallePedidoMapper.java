package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoFullResponse;
import com.aluracursos.buensaborbp.dto.DetallePedidoFullResponse;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturado;
import com.aluracursos.buensaborbp.entity.DetallePedido;
import org.hibernate.Hibernate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class DetallePedidoMapper {

    @Autowired
    protected ArticuloManufacturadoMapper articuloManufacturadoMapper;

    // Precio al que se vendió (subtotal congelado / cantidad), no el precio de hoy del artículo:
    // si el precio cambió después, el historial mostraba un unitario que no cerraba con el subtotal.
    @Mapping(target = "precioUnitario", expression = "java(precioUnitarioVendido(entity))")
    @Mapping(target = "nombreArticulo", source = "articulo.nombre")
    @Mapping(target = "idArticulo", source = "articulo.id")
    @Mapping(target = "articuloManufacturado", expression = "java(mapArticuloManufacturado(entity))")
    public abstract DetallePedidoFullResponse toFullResponse(DetallePedido entity);

    protected java.math.BigDecimal precioUnitarioVendido(DetallePedido entity) {
        if (entity.getSubtotal() == null || entity.getCantidad() == null || entity.getCantidad() == 0) {
            return null;
        }
        return entity.getSubtotal().divide(java.math.BigDecimal.valueOf(entity.getCantidad()), 2, java.math.RoundingMode.HALF_UP);
    }

    // El detalle de pedido referencia un Articulo genérico (puede ser insumo, manufacturado
    // o promoción); esta respuesta solo expone datos del plato cuando efectivamente lo es.
    // Hibernate.unproxy es necesario: "articulo" es LAZY sobre una jerarquía JOINED, así que
    // sin desproxyearlo primero el "instanceof" ve el proxy base y nunca matchea el subtipo
    // real (el detalle quedaba con articuloManufacturado siempre en null).
    protected ArticuloManufacturadoFullResponse mapArticuloManufacturado(DetallePedido entity) {
        if (Hibernate.unproxy(entity.getArticulo()) instanceof ArticuloManufacturado manufacturado) {
            return articuloManufacturadoMapper.toFullResponse(manufacturado);
        }
        return null;
    }
}
