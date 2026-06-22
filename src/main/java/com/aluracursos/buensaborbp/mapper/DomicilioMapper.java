package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.*;
import com.aluracursos.buensaborbp.entity.*;
import org.mapstruct.Mapper; 
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import java.util.List;

@Mapper(componentModel = "spring", uses = {CiudadMapper.class})
public interface DomicilioMapper {
    @Mapping(source = "ciudad.nombre", target = "ciudad")
    DomicilioBaseResponse toBaseResponse(Domicilio entity);

    @Mapping(source = "ciudad.nombre", target = "ciudad")
    DomicilioFullResponse toFullResponse(Domicilio entity);

    Domicilio toEntity(DomicilioRequest request);

    List<DomicilioBaseResponse> toBaseResponseList(List<Domicilio> entities);

    @Mapping(target = "id", ignore = true) 
    @Mapping(target = "ciudad", ignore = true)       
    void updateEntityFromDto(DomicilioRequest request, @MappingTarget Domicilio entity);
}