package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.EmpresaResponse;
import com.aluracursos.buensaborbp.entity.Empresa;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmpresaMapper {
    EmpresaResponse toResponse(Empresa entity);
}
