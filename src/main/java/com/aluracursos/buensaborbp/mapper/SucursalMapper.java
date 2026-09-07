package com.aluracursos.buensaborbp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;
import com.aluracursos.buensaborbp.entity.Sucursal;
import com.aluracursos.buensaborbp.dto.SucursalResponse;

@Mapper(componentModel = "spring")
public interface SucursalMapper {

    @Mapping(target = "id", source = "id_sucursal")
    @Mapping(target = "direccionCompleta", expression = "java(direccionCompleta(s))")
    SucursalResponse toResponse(Sucursal s);

    List<SucursalResponse> toResponseList(List<Sucursal> sucursales);

    // La sucursal puede no tener domicilio cargado todavía (p. ej. recién creada).
    default String direccionCompleta(Sucursal s) {
        if (s.getDomicilio() == null) {
            return null;
        }
        String ciudad = s.getDomicilio().getCiudad() != null ? s.getDomicilio().getCiudad().getNombre() : null;
        return s.getDomicilio().getCalle() + " " + s.getDomicilio().getNumero() + (ciudad != null ? ", " + ciudad : "");
    }
}
