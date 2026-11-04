package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.ItemCocinaResponse;
import com.aluracursos.buensaborbp.dto.PedidoBaseResponse;
import com.aluracursos.buensaborbp.dto.PedidoCajaResponse;
import com.aluracursos.buensaborbp.dto.PedidoCocinaResponse;
import com.aluracursos.buensaborbp.dto.PedidoEntregaResponse;
import com.aluracursos.buensaborbp.dto.PedidoFullResponse;
import com.aluracursos.buensaborbp.entity.Articulo;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturado;
import com.aluracursos.buensaborbp.entity.DetallePedido;
import com.aluracursos.buensaborbp.entity.Pedido;
import com.aluracursos.buensaborbp.entity.Promocion;
import org.hibernate.Hibernate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * No usa ClienteMapper para no crear una dependencia circular:
 * ClienteMapper ya depende de PedidoMapper (Cliente.pedidos). Por eso el campo
 * "cliente" se arma a mano con solo los datos básicos, sin re-embeber su
 * historial de pedidos ni su foto.
 */
@Mapper(componentModel = "spring", uses = {DetallePedidoMapper.class})
public interface PedidoMapper {
    // Para el Cliente (Mis Órdenes)
    @Mapping(target = "estadoPedido", source = "estado")
    @Mapping(target = "detalles", source = "detallePedidos")
    @Mapping(target = "idFactura", source = "factura.id")
    PedidoBaseResponse toBaseResponse(Pedido pedido);

    List<PedidoBaseResponse> toBaseResponseList(List<Pedido> pedidos);

    // Para el Cocinero (Pantalla de Cocina)
    @Mapping(target = "estadoPedido", source = "estado")
    @Mapping(target = "cliente", expression = "java(pedido.getCliente() == null ? null : new com.aluracursos.buensaborbp.dto.ClienteBaseResponse(pedido.getCliente().getId_usuario(), pedido.getCliente().getNombre(), pedido.getCliente().getApellido(), null, pedido.getCliente().getEmail(), pedido.getCliente().getTelefono(), null, null))")
    @Mapping(target = "items", source = "detallePedidos")
    PedidoCocinaResponse toCocinaResponse(Pedido pedido);

    List<PedidoCocinaResponse> toCocinaResponseList(List<Pedido> pedidos);

    List<ItemCocinaResponse> toItemsCocina(List<DetallePedido> detalles);

    default ItemCocinaResponse toItemCocina(DetallePedido detalle) {
        return itemCocina(detalle.getArticulo(), detalle.getCantidad());
    }

    // DetallePedido.articulo es LAZY: sin unproxy el instanceof contra las subclases falla.
    private static ItemCocinaResponse itemCocina(Articulo articuloLazy, int cantidad) {
        Articulo articulo = (Articulo) Hibernate.unproxy(articuloLazy);
        if (articulo instanceof Promocion promocion) {
            List<ItemCocinaResponse> incluye = promocion.getPromocionDetalle().stream()
                .filter(d -> d.getArticulos() != null)
                .map(d -> itemCocina(d.getArticulos(), d.getCantidad() * cantidad))
                .toList();
            return new ItemCocinaResponse(promocion.getNombre(), cantidad, null, incluye);
        }
        String preparacion = articulo instanceof ArticuloManufacturado manufacturado
            && manufacturado.getPreparacion() != null && !manufacturado.getPreparacion().isBlank()
                ? manufacturado.getPreparacion() : null;
        return new ItemCocinaResponse(articulo.getNombre(), cantidad, preparacion, List.of());
    }

    // Detalle completo
    @Mapping(target = "estadoPedido", source = "estado")
    @Mapping(target = "cliente", expression = "java(pedido.getCliente() == null ? null : new com.aluracursos.buensaborbp.dto.ClienteBaseResponse(pedido.getCliente().getId_usuario(), pedido.getCliente().getNombre(), pedido.getCliente().getApellido(), null, pedido.getCliente().getEmail(), pedido.getCliente().getTelefono(), null, null))")
    @Mapping(target = "repartidor", expression = "java(pedido.getRepartidor() == null ? null : new com.aluracursos.buensaborbp.dto.RepartidorResponse(pedido.getRepartidor().getId_usuario(), pedido.getRepartidor().getNombre(), pedido.getRepartidor().getApellido()))")
    PedidoFullResponse toFullResponse(Pedido pedido);

    // Para el Repartidor (pool disponible / mis entregas)
    @Mapping(target = "estadoPedido", source = "estado")
    PedidoEntregaResponse toEntregaResponse(Pedido pedido);

    List<PedidoEntregaResponse> toEntregaResponseList(List<Pedido> pedidos);

    // Para el Cajero (pedidos por aceptar / listos para retirar en el mostrador)
    @Mapping(target = "estadoPedido", source = "estado")
    @Mapping(target = "cliente", expression = "java(pedido.getCliente() == null ? null : new com.aluracursos.buensaborbp.dto.ClienteBaseResponse(pedido.getCliente().getId_usuario(), pedido.getCliente().getNombre(), pedido.getCliente().getApellido(), null, pedido.getCliente().getEmail(), pedido.getCliente().getTelefono(), null, null))")
    PedidoCajaResponse toCajaResponse(Pedido pedido);

    List<PedidoCajaResponse> toCajaResponseList(List<Pedido> pedidos);
}
