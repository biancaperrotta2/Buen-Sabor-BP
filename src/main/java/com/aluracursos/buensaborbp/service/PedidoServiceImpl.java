package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.PedidoBaseResponse;
import com.aluracursos.buensaborbp.dto.PedidoCocinaResponse;
import com.aluracursos.buensaborbp.dto.PedidoFullResponse;
import com.aluracursos.buensaborbp.dto.PedidoRequest;
import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.entity.Enums.EstadoPago;
import com.aluracursos.buensaborbp.entity.Pedido;
import com.aluracursos.buensaborbp.mapper.PedidoMapper;
import com.aluracursos.buensaborbp.repository.PedidoRepository;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aluracursos.buensaborbp.repository.ArticuloRepository;
import com.aluracursos.buensaborbp.repository.ClienteRepository;
import com.aluracursos.buensaborbp.repository.DomicilioRepository;
import com.aluracursos.buensaborbp.entity.Articulo;
import com.aluracursos.buensaborbp.entity.Cliente;
import com.aluracursos.buensaborbp.entity.DetallePedido;
import com.aluracursos.buensaborbp.entity.Domicilio;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoServiceImpl extends BaseServiceImpl<Pedido, Long> implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoMapper pedidoMapper;
    private final ArticuloRepository articuloRepository;
    private final ClienteRepository clienteRepository;
    private final DomicilioRepository domicilioRepository;
    private final FacturaService facturaService;

    public PedidoServiceImpl(PedidoRepository pedidoRepository, 
                             PedidoMapper pedidoMapper, 
                             ArticuloRepository articuloRepository,
                             ClienteRepository clienteRepository,
                             DomicilioRepository domicilioRepository,
                             FacturaService facturaService) {
        super(pedidoRepository);
        this.pedidoRepository = pedidoRepository;
        this.pedidoMapper = pedidoMapper;
        this.articuloRepository = articuloRepository;
        this.clienteRepository = clienteRepository;
        this.domicilioRepository = domicilioRepository;
        this.facturaService = facturaService;
    }

    @Override
    @Transactional
    public PedidoFullResponse crearPedido(PedidoRequest request) {
        // 1. Obtener Cliente desde Spring Security (Email del Token JWT)
        String emailUsuario = SecurityContextHolder.getContext().getAuthentication().getName();
        Cliente cliente = clienteRepository.findByEmail(emailUsuario)
            .orElseThrow(() -> new EntityNotFoundException("Cliente no vinculado a la cuenta: " + emailUsuario));

        // 2. Validar Domicilio
        Domicilio domicilio = domicilioRepository.findById(request.idDomicilio())
            .orElseThrow(() -> new EntityNotFoundException("Domicilio no encontrado"));

        // 3. Construir Pedido
        Pedido pedido = Pedido.builder()
            .fechaPedido(LocalDate.now())
            .horaEstimadaFinalizacion(LocalTime.now().plusMinutes(30))
            .estado(Estado.PENDIENTE)
            .estadoPago(EstadoPago.PENDIENTE) // Por defecto pendiente hasta que MP confirme
            .tipoEnvio(request.tipoEnvio())
            .formaPago(request.formaPago())
            .aclaracionDomicilio(request.aclaracionDomicilio())
            .cliente(cliente)
            .domicilio(domicilio)
            .domicilioSnapshot(domicilio.getCalle() + " " + domicilio.getNumero())
            .detallePedidos(new ArrayList<>())
            .build();

        // 4. Mapear Detalles y calcular subtotales
        request.detallePedidos().forEach(detRequest -> {
            Articulo articulo = articuloRepository.findById(detRequest.idArticulo())
                .orElseThrow(() -> new EntityNotFoundException("Articulo no encontrado"));
            
            DetallePedido detalle = DetallePedido.builder()
                .articulo(articulo)
                .cantidad(detRequest.cantidad())
                .subtotal(articulo.getPrecioVenta().multiply(BigDecimal.valueOf(detRequest.cantidad())))
                .pedido(pedido)
                .build();
            
            pedido.getDetallePedidos().add(detalle);
        });

        // 5. Lógica de negocio de tu entidad
        pedido.calcularTotal();
        pedido.calcularCostoTotal();
        pedido.reservarStock(); 

        Pedido guardado = pedidoRepository.save(pedido);
        return pedidoMapper.toFullResponse(guardado);
    }

    @Override
    @Transactional
    public PedidoFullResponse cambiarEstado(Long id, Estado nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));

        Estado estadoAnterior = pedido.getEstado();

        // --- LÓGICA DE STOCK ---
        if (nuevoEstado == Estado.EN_PROCESO && estadoAnterior == Estado.PENDIENTE) {
            pedido.confirmarStock();
        }
        
        if (nuevoEstado == Estado.CANCELADO && estadoAnterior != Estado.CANCELADO) {
            pedido.liberarStock();
        }

        pedido.setEstado(nuevoEstado);

        // --- FACTURACIÓN AUTOMÁTICA ---
        // Si el pedido se entrega y el pago ya fue confirmado (PAGADO)
        if (nuevoEstado == Estado.ENTREGADO && pedido.getEstadoPago() == EstadoPago.PAGADO) {
            facturaService.generarFacturaDesdePedido(pedido.getId());
        }

        return pedidoMapper.toFullResponse(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoCocinaResponse> listarParaCocina() {
        // El cocinero ve lo que hay que cocinar y lo que está listo para salir
        List<Estado> estadosCocina = List.of(
            Estado.PENDIENTE, 
            Estado.EN_PROCESO, 
            Estado.PREPARADO
        );
        List<Pedido> pedidos = pedidoRepository.findByEstadoPedidoInOrderByHoraEstimadaFinalizacionAsc(estadosCocina);
        return pedidoMapper.toCocinaResponseList(pedidos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoBaseResponse> historialCliente(Long clienteId) {
        List<Pedido> pedidos = pedidoRepository.findByClienteIdOrderByFechaPedidoDesc(clienteId);
        return pedidoMapper.toBaseResponseList(pedidos);
    }
}