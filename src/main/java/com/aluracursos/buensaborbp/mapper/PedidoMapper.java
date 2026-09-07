package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.PedidoBaseResponse;
import com.aluracursos.buensaborbp.dto.PedidoCocinaResponse;
import com.aluracursos.buensaborbp.dto.PedidoEntregaResponse;
import com.aluracursos.buensaborbp.dto.PedidoFullResponse;
import com.aluracursos.buensaborbp.entity.Pedido;
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
    PedidoBaseResponse toBaseResponse(Pedido pedido);

    List<PedidoBaseResponse> toBaseResponseList(List<Pedido> pedidos);

    // Para el Cocinero (Pantalla de Cocina)
    @Mapping(target = "estadoPedido", source = "estado")
    @Mapping(target = "cliente", expression = "java(pedido.getCliente() == null ? null : new com.aluracursos.buensaborbp.dto.ClienteBaseResponse(pedido.getCliente().getId_usuario(), pedido.getCliente().getNombre(), pedido.getCliente().getApellido(), null, pedido.getCliente().getEmail(), pedido.getCliente().getTelefono(), null))")
    PedidoCocinaResponse toCocinaResponse(Pedido pedido);

    List<PedidoCocinaResponse> toCocinaResponseList(List<Pedido> pedidos);

    // Detalle completo
    @Mapping(target = "estadoPedido", source = "estado")
    @Mapping(target = "cliente", expression = "java(pedido.getCliente() == null ? null : new com.aluracursos.buensaborbp.dto.ClienteBaseResponse(pedido.getCliente().getId_usuario(), pedido.getCliente().getNombre(), pedido.getCliente().getApellido(), null, pedido.getCliente().getEmail(), pedido.getCliente().getTelefono(), null))")
    @Mapping(target = "repartidor", expression = "java(pedido.getRepartidor() == null ? null : new com.aluracursos.buensaborbp.dto.RepartidorResponse(pedido.getRepartidor().getId_usuario(), pedido.getRepartidor().getNombre(), pedido.getRepartidor().getApellido()))")
    PedidoFullResponse toFullResponse(Pedido pedido);

    // Para el Repartidor (pool disponible / mis entregas)
    @Mapping(target = "estadoPedido", source = "estado")
    PedidoEntregaResponse toEntregaResponse(Pedido pedido);

    List<PedidoEntregaResponse> toEntregaResponseList(List<Pedido> pedidos);
}
