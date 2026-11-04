package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.*;
import com.aluracursos.buensaborbp.entity.Empleado;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring", uses = {ImagenUsuarioMapper.class})
public interface EmpleadoMapper {
    @Mapping(target = "id", source = "id_usuario")
    @Mapping(target = "cargo", source = "rol")
    EmpleadoBaseResponse toBaseResponse(Empleado entity);

    @Mapping(target = "id", source = "id_usuario")
    @Mapping(target = "fotoPerfilEmpleado", expression = "java(entity.getImagenes().isEmpty() ? null : new com.aluracursos.buensaborbp.dto.ImagenUsuarioResponse(entity.getImagenes().get(0).getId(), entity.getImagenes().get(0).getUrlImagen(), entity.getImagenes().get(0).getDenominacion()))")
    EmpleadoFullResponse toFullResponse(Empleado entity);

    Empleado toEntity(EmpleadoRequest request);

    List<EmpleadoBaseResponse> toBaseResponseList(List<Empleado> entities);

    @org.mapstruct.Mapping(target = "id_usuario", ignore = true)
    @org.mapstruct.Mapping(target = "password", ignore = true)
    void updateEntity(EmpleadoRequest request, @org.mapstruct.MappingTarget Empleado entity);

}
