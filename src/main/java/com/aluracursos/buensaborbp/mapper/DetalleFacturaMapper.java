package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.DetalleFacturaResponse;
import com.aluracursos.buensaborbp.entity.DetalleFactura;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DetalleFacturaMapper {
    DetalleFacturaResponse toResponse(DetalleFactura entity);
}
