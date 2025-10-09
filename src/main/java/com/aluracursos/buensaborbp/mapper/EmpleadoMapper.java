package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.*;
import com.aluracursos.buensaborbp.entity.Empleado;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ImagenUsuarioMapper.class})
public interface EmpleadoMapper {
    EmpleadoBaseResponse toBaseResponse(Empleado entity);

    EmpleadoFullResponse toFullResponse(Empleado entity);

    Empleado toEntity(EmpleadoRequest request);

    List<EmpleadoBaseResponse> toBaseResponseList(List<Empleado> entities);

}
