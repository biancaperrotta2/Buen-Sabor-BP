package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.FacturaBaseResponse;
import com.aluracursos.buensaborbp.dto.FacturaFullResponse;
import com.aluracursos.buensaborbp.entity.Factura;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ClienteMapper.class, EmpresaMapper.class, DetalleFacturaMapper.class})
public interface FacturaMapper {
    FacturaBaseResponse toBaseResponse(Factura entity);

    FacturaFullResponse toFullResponse(Factura entity);

}
