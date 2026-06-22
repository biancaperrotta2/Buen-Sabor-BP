package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.FacturaBaseResponse;
import com.aluracursos.buensaborbp.dto.FacturaFullResponse;
import com.aluracursos.buensaborbp.entity.Factura;
import org.mapstruct.Mapper;
import java.util.List;
import com.aluracursos.buensaborbp.entity.DetalleFactura;
import com.aluracursos.buensaborbp.dto.DetalleFacturaResponse;


@Mapper(componentModel = "spring")
public interface FacturaMapper {

    FacturaBaseResponse toBaseResponse(Factura factura);

    List<FacturaBaseResponse> toBaseResponseList(List<Factura> facturas);

    FacturaFullResponse toFullResponse(Factura factura);

    DetalleFacturaResponse toDetalleResponse(DetalleFactura detalle);
    
    List<DetalleFacturaResponse> toDetalleResponseList(List<DetalleFactura> detalles);
}