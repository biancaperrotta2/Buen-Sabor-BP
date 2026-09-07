package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.*;
import com.aluracursos.buensaborbp.entity.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DomicilioMapper.class, ImagenUsuarioMapper.class, PedidoMapper.class})
public interface ClienteMapper {
    @Mapping(target = "id", source = "id_usuario")
    @Mapping(target = "fotoPerfilCliente", expression = "java(entity.getImagenes().isEmpty() ? null : new com.aluracursos.buensaborbp.dto.ImagenUsuarioResponse(entity.getImagenes().get(0).getId(), entity.getImagenes().get(0).getUrlImagen(), entity.getImagenes().get(0).getDenominacion()))")
    @Mapping(target = "pedidos", source = "listaPedido")
    ClienteBaseResponse toBaseResponse(Cliente entity);

    @Mapping(target = "id", source = "id_usuario")
    @Mapping(target = "fotoPerfilCliente", expression = "java(entity.getImagenes().isEmpty() ? null : new com.aluracursos.buensaborbp.dto.ImagenUsuarioResponse(entity.getImagenes().get(0).getId(), entity.getImagenes().get(0).getUrlImagen(), entity.getImagenes().get(0).getDenominacion()))")
    ClienteFullResponse toFullResponse(Cliente entity);

    Cliente toEntity(ClienteRequest request);

    List<ClienteBaseResponse> toBaseResponseList(List<Cliente> entities);
    
    @Mapping(target = "id_usuario", ignore = true)
    @Mapping(target = "password", ignore = true) 
    void updateEntity(ClienteRequest request, @org.mapstruct.MappingTarget Cliente entity);
}
