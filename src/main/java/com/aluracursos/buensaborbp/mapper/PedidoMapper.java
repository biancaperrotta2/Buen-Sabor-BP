package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.*;
import com.aluracursos.buensaborbp.entity.Pedido;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PedidoMapper {
    // Para el Cliente (Mis Órdenes)
    PedidoBaseResponse toBaseResponse(Pedido pedido);
    List<PedidoBaseResponse> toBaseResponseList(List<Pedido> pedidos);

    // Para el Cocinero (Pantalla de Cocina)
    PedidoCocinaResponse toCocinaResponse(Pedido pedido);
    List<PedidoCocinaResponse> toCocinaResponseList(List<Pedido> pedidos);

    // Detalle completo
    PedidoFullResponse toFullResponse(Pedido pedido);
}
