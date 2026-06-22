package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.FacturaBaseResponse;
import com.aluracursos.buensaborbp.dto.FacturaFullResponse;
import com.aluracursos.buensaborbp.entity.Factura;
import java.util.List;

public interface FacturaService extends BaseService<Factura, Long> {
    
    List<FacturaBaseResponse> mostrarTodasLasFacturas();

    FacturaFullResponse buscarDetalleFactura(Long id);

    FacturaFullResponse generarFacturaDesdePedido(Long pedidoId);

    FacturaFullResponse buscarPorPedidoId(Long pedidoId);
}