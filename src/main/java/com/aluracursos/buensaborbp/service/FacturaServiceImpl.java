package com.aluracursos.buensaborbp.service;

import org.springframework.stereotype.Service;

import com.aluracursos.buensaborbp.entity.DetalleFactura;
import com.aluracursos.buensaborbp.entity.Factura;
import org.springframework.transaction.annotation.Transactional;
import com.aluracursos.buensaborbp.dto.FacturaBaseResponse;
import com.aluracursos.buensaborbp.dto.FacturaFullResponse;
import com.aluracursos.buensaborbp.entity.Pedido;
import com.aluracursos.buensaborbp.repository.FacturaRepository;
import com.aluracursos.buensaborbp.repository.PedidoRepository;
import com.aluracursos.buensaborbp.mapper.FacturaMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class FacturaServiceImpl extends BaseServiceImpl<Factura, Long> implements FacturaService {

    private final FacturaRepository facturaRepository;
    private final PedidoRepository pedidoRepository;
    private final FacturaMapper facturaMapper;

    public FacturaServiceImpl(FacturaRepository facturaRepository, 
                              PedidoRepository pedidoRepository, 
                              FacturaMapper facturaMapper) {
        super(facturaRepository);
        this.facturaRepository = facturaRepository;
        this.pedidoRepository = pedidoRepository;
        this.facturaMapper = facturaMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaBaseResponse> mostrarTodasLasFacturas() {
        List<Factura> facturas = facturaRepository.findAll();
        return facturaMapper.toBaseResponseList(facturas);
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaFullResponse buscarDetalleFactura(Long id) {
        Factura factura = facturaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada ID: " + id));
        return facturaMapper.toFullResponse(factura);
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaFullResponse buscarPorPedidoId(Long pedidoId) {
        Factura factura = facturaRepository.findByPedidoId(pedidoId)
            .orElseThrow(() -> new EntityNotFoundException("No existe factura para el pedido: " + pedidoId));
        return facturaMapper.toFullResponse(factura);
    }

    @Override
    @Transactional
    public FacturaFullResponse generarFacturaDesdePedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));

        if (facturaRepository.existsByPedidoId(pedidoId)) {
            throw new IllegalStateException("El pedido ya tiene una factura generada.");
        }

        // Construcción de la Factura
        Factura factura = Factura.builder()
            .fechaFacturacion(LocalDate.now())
            .pedido(pedido)
            .totalVenta(pedido.getTotal()) 
            .formaPago(pedido.getFormaPago())  
            .estadoPago(pedido.getEstadoPago()) 
            .numeroComprobante(System.currentTimeMillis()) 
            .detallesFactura(new ArrayList<>())
            .build();

        pedido.getDetallePedidos().forEach(dp -> {
            DetalleFactura df = DetalleFactura.builder()
                .cantidad(dp.getCantidad())
                .subTotal(dp.getSubtotal())
                .factura(factura)
                .articulo(dp.getArticulo())
                .build();
            factura.getDetallesFactura().add(df);
        });

        return facturaMapper.toFullResponse(facturaRepository.save(factura));
    }
}