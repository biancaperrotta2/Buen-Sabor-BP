package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.*;
import com.aluracursos.buensaborbp.entity.Pedido;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DetallePedidoMapper.class, })
public interface PedidoMapper {
    PedidoBaseResponse toBaseResponse(Pedido entity);

    PedidoFullResponse toFullResponse(Pedido entity);

    PedidoCocinaResponse toCocinaResponse(Pedido entity);

    Pedido toEntity(PedidoRequest request);

    List<PedidoBaseResponse> toBaseResponseList(List<Pedido> entities);

    List<PedidoCocinaResponse> toCocinaResponseList(List<Pedido> entities);
}
