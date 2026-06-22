package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.CiudadResponse;
import com.aluracursos.buensaborbp.entity.Ciudad;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CiudadMapper {
    @Mapping(target = "idProvincia", source = "provincia.id")
    CiudadResponse toResponse(Ciudad ciudad);
    
    List<CiudadResponse> toResponseList(List<Ciudad> ciudades);
}