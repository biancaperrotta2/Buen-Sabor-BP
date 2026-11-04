package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.FacturaBaseResponse;
import com.aluracursos.buensaborbp.dto.FacturaFullResponse;
import com.aluracursos.buensaborbp.entity.DetalleFactura;
import com.aluracursos.buensaborbp.entity.Factura;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring", uses = {ClienteMapper.class, EmpresaMapper.class, DetalleFacturaMapper.class})
public interface FacturaMapper {

    @Mapping(target = "idFactura", source = "id")
    @Mapping(target = "idPedido", source = "pedido.id")
    @Mapping(target = "idCliente", source = "pedido.cliente.id_usuario")
    @Mapping(target = "nombreCliente", expression = "java(factura.getPedido().getCliente() == null ? null : factura.getPedido().getCliente().getNombre() + \" \" + factura.getPedido().getCliente().getApellido())")
    @Mapping(target = "fecha", source = "fechaFacturacion")
    @Mapping(target = "total", source = "totalVenta")
    @Mapping(target = "numeroFactura", source = "numeroComprobante")
    FacturaBaseResponse toBaseResponse(Factura factura);

    List<FacturaBaseResponse> toBaseResponseList(List<Factura> facturas);

    @Mapping(target = "numeroFactura", source = "numeroComprobante")
    @Mapping(target = "fecha", expression = "java(factura.getFechaFacturacion() == null ? null : factura.getFechaFacturacion().atStartOfDay())")
    @Mapping(target = "cliente", source = "pedido.cliente")
    @Mapping(target = "empresa", source = "pedido.sucursal.empresa")
    @Mapping(target = "total", source = "totalVenta")
    @Mapping(target = "metodoPago", source = "formaPago")
    // La Factura no guarda el desglose (solo totalVenta): envío/descuento vienen del Pedido
    // asociado, y el subtotal se reconstruye sumando las líneas de detalle.
    @Mapping(target = "envio", source = "pedido.gastosEnvio")
    @Mapping(target = "descuento", source = "pedido.descuento")
    @Mapping(target = "subtotal", expression = "java(sumarDetalles(factura))")
    FacturaFullResponse toFullResponse(Factura factura);

    default BigDecimal sumarDetalles(Factura factura) {
        if (factura.getDetallesFactura() == null) {
            return BigDecimal.ZERO;
        }
        return factura.getDetallesFactura().stream()
                .map(DetalleFactura::getSubTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
