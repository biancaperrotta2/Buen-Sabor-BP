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

    @Mapping(target = "precioUnitario", source = "articulo.precioVenta")
    @Mapping(target = "nombreArticulo", source = "articulo.nombre")
    @Mapping(target = "articuloManufacturado", expression = "java(mapArticuloManufacturado(entity))")
    public abstract DetallePedidoFullResponse toFullResponse(DetallePedido entity);

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
