package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.FacturaBaseResponse;
import com.aluracursos.buensaborbp.dto.FacturaFullResponse;
import com.aluracursos.buensaborbp.dto.PageResponse;
import com.aluracursos.buensaborbp.entity.Factura;
import org.springframework.data.domain.Pageable;

public interface FacturaService extends BaseService<Factura, Long> {

    PageResponse<FacturaBaseResponse> mostrarTodasLasFacturas(String busqueda, Pageable pageable);

    FacturaFullResponse buscarDetalleFactura(Long id);

    FacturaFullResponse generarFacturaDesdePedido(Long pedidoId);

    FacturaFullResponse buscarPorPedidoId(Long pedidoId);
}