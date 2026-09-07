package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.UsuarioRequest;
import com.aluracursos.buensaborbp.dto.UsuarioResponse;
import com.aluracursos.buensaborbp.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    @Mapping(target = "id", source = "id_usuario")
    UsuarioResponse toResponse(Usuario entity);

    Usuario toEntity(UsuarioRequest request);
}
