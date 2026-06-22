package com.aluracursos.buensaborbp.mapper;

import org.mapstruct.Mapper;
import com.aluracursos.buensaborbp.entity.Provincia;
import com.aluracursos.buensaborbp.dto.ProvinciaResponse;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProvinciaMapper {
    ProvinciaResponse toResponse(Provincia provincia);
    List<ProvinciaResponse> toResponseList(List<Provincia> provincias);
}