package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.DetallePedidoFullResponse;
import com.aluracursos.buensaborbp.dto.DetallePedidoRequest;
import com.aluracursos.buensaborbp.entity.DetallePedido;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ArticuloManufacturadoMapper.class})
public interface DetallePedidoMapper {
    DetallePedidoFullResponse toFullResponse(DetallePedido entity);

    DetallePedido toEntity(DetallePedidoRequest request);
}
