package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.UsuarioRequest;
import com.aluracursos.buensaborbp.dto.UsuarioResponse;
import com.aluracursos.buensaborbp.entity.Usuario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    UsuarioResponse toResponse(Usuario entity);

    Usuario toEntity(UsuarioRequest request);
}
