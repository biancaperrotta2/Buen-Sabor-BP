package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.*;
import com.aluracursos.buensaborbp.entity.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DomicilioMapper.class, ImagenUsuarioMapper.class, PedidoMapper.class})
public interface ClienteMapper {
    ClienteBaseResponse toBaseResponse(Cliente entity);

    ClienteFullResponse toFullResponse(Cliente entity);

    Cliente toEntity(ClienteRequest request);

    List<ClienteBaseResponse> toBaseResponseList(List<Cliente> entities);
    
    @Mapping(target = "id_usuario", ignore = true)
    @Mapping(target = "password", ignore = true) 
    void updateEntity(ClienteRequest request, @org.mapstruct.MappingTarget Cliente entity);
}
