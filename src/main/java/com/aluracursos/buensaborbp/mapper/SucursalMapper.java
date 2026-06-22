package com.aluracursos.buensaborbp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;
import com.aluracursos.buensaborbp.entity.Sucursal;
import com.aluracursos.buensaborbp.dto.SucursalResponse;

@Mapper(componentModel = "spring")
public interface SucursalMapper {
    
    @Mapping(target = "id", source = "id_sucursal")
    @Mapping(target = "direccionCompleta", expression = "java(s.getDomicilio().getCalle() + \" \" + s.getDomicilio().getNumero() + \", \" + s.getDomicilio().getLocalidad().getNombre())")
    SucursalResponse toResponse(Sucursal s);
    
    List<SucursalResponse> toResponseList(List<Sucursal> sucursales);
}
