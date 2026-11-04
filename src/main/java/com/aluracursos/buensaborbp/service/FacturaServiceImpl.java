package com.aluracursos.buensaborbp.service;

import org.springframework.stereotype.Service;

import com.aluracursos.buensaborbp.entity.DetalleFactura;
import com.aluracursos.buensaborbp.entity.Factura;
import org.springframework.transaction.annotation.Transactional;
import com.aluracursos.buensaborbp.dto.FacturaBaseResponse;
import com.aluracursos.buensaborbp.dto.FacturaFullResponse;
import com.aluracursos.buensaborbp.dto.PageResponse;
import com.aluracursos.buensaborbp.entity.Pedido;
import com.aluracursos.buensaborbp.repository.FacturaRepository;
import com.aluracursos.buensaborbp.repository.PedidoRepository;
import com.aluracursos.buensaborbp.mapper.FacturaMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Pageable;
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
    public PageResponse<FacturaBaseResponse> mostrarTodasLasFacturas(String busqueda, Pageable pageable) {
        String filtro = busqueda != null ? busqueda.trim() : "";
        return PageResponse.de(facturaRepository.buscar(filtro, pageable), facturaMapper::toBaseResponse);
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

        Factura facturaGuardada = facturaRepository.save(factura);
        // Pedido.factura es el otro lado de esta relación (columna factura_id, en la
        // tabla pedido): sin setearlo, el pedido nunca queda enterado de su propia
        // factura y cualquier consulta que lo recorra desde ahí (p. ej. el idFactura
        // que ve el cliente en "Mis pedidos") lo ve como null aunque la factura exista.
        pedido.setFactura(facturaGuardada);
        pedidoRepository.save(pedido);

        return facturaMapper.toFullResponse(facturaGuardada);
    }
}