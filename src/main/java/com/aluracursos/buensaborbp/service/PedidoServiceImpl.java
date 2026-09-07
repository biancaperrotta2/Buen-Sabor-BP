package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.PedidoBaseResponse;
import com.aluracursos.buensaborbp.dto.PedidoCocinaResponse;
import com.aluracursos.buensaborbp.dto.PedidoEntregaResponse;
import com.aluracursos.buensaborbp.dto.PedidoFullResponse;
import com.aluracursos.buensaborbp.dto.PedidoRequest;
import com.aluracursos.buensaborbp.dto.PreferenciaPagoResponse;
import com.aluracursos.buensaborbp.entity.Carrito;
import com.aluracursos.buensaborbp.entity.DetalleMercadoPago;
import com.aluracursos.buensaborbp.entity.Empleado;
import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.entity.Enums.EstadoPago;
import com.aluracursos.buensaborbp.entity.Enums.FormaPago;
import com.aluracursos.buensaborbp.entity.Enums.TipoEnvio;
import com.aluracursos.buensaborbp.entity.Pedido;
import com.aluracursos.buensaborbp.mapper.PedidoMapper;
import com.aluracursos.buensaborbp.repository.PedidoRepository;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aluracursos.buensaborbp.repository.CarritoRepository;
import com.aluracursos.buensaborbp.repository.ClienteRepository;
import com.aluracursos.buensaborbp.repository.DomicilioRepository;
import com.aluracursos.buensaborbp.repository.EmpleadoRepository;
import com.aluracursos.buensaborbp.repository.SucursalRepository;
import com.aluracursos.buensaborbp.entity.Articulo;
import com.aluracursos.buensaborbp.entity.Cliente;
import com.aluracursos.buensaborbp.entity.DetallePedido;
import com.aluracursos.buensaborbp.entity.Domicilio;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PedidoServiceImpl extends BaseServiceImpl<Pedido, Long> implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoMapper pedidoMapper;
    private final CarritoRepository carritoRepository;
    private final ClienteRepository clienteRepository;
    private final DomicilioRepository domicilioRepository;
    private final EmpleadoRepository empleadoRepository;
    private final SucursalRepository sucursalRepository;
    private final FacturaService facturaService;

    // Transiciones que puede disparar el flujo genérico (ADMIN/CAJERO/COCINERO vía
    // PUT /pedidos/{id}/estado). EN_CAMINO queda fuera a propósito: solo se llega ahí
    // asignando un repartidor (ver tomarPedido), nunca seteando el estado a mano.
    private static final Map<Estado, Set<Estado>> TRANSICIONES_VALIDAS = Map.of(
            Estado.PENDIENTE, Set.of(Estado.EN_PROCESO, Estado.CANCELADO),
            Estado.EN_PROCESO, Set.of(Estado.DEMORADO, Estado.PREPARADO, Estado.CANCELADO),
            Estado.DEMORADO, Set.of(Estado.DEMORADO, Estado.PREPARADO, Estado.CANCELADO),
            Estado.PREPARADO, Set.of(Estado.ENTREGADO, Estado.CANCELADO),
            Estado.EN_CAMINO, Set.of(Estado.CANCELADO),
            Estado.ENTREGADO, Set.of(),
            Estado.CANCELADO, Set.of()
    );

    @Value("${mercadopago.notification-url:}")
    private String notificationUrl;

    @Value("${mercadopago.back-url-success:}")
    private String backUrlSuccess;

    @Value("${mercadopago.back-url-pending:}")
    private String backUrlPending;

    @Value("${mercadopago.back-url-failure:}")
    private String backUrlFailure;

    public PedidoServiceImpl(PedidoRepository pedidoRepository,
                             PedidoMapper pedidoMapper,
                             CarritoRepository carritoRepository,
                             ClienteRepository clienteRepository,
                             DomicilioRepository domicilioRepository,
                             EmpleadoRepository empleadoRepository,
                             SucursalRepository sucursalRepository,
                             FacturaService facturaService) {
        super(pedidoRepository);
        this.pedidoRepository = pedidoRepository;
        this.pedidoMapper = pedidoMapper;
        this.carritoRepository = carritoRepository;
        this.clienteRepository = clienteRepository;
        this.domicilioRepository = domicilioRepository;
        this.empleadoRepository = empleadoRepository;
        this.sucursalRepository = sucursalRepository;
        this.facturaService = facturaService;
    }

    @Override
    @Transactional
    public PedidoFullResponse crearPedido(PedidoRequest request) {
        // 1. Obtener Cliente desde Spring Security. El subject del JWT es Usuario.usuario
        // (ver TokenService.generarToken), no el email.
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Cliente cliente = clienteRepository.findByUsuario(username)
            .orElseThrow(() -> new EntityNotFoundException("Cliente no vinculado a la cuenta: " + username));

        // 2. El pedido se arma con lo que haya en el carrito del cliente al momento de
        // confirmar la orden (ver CarritoController); no se puede pedir sin haber
        // agregado antes al menos un artículo.
        Carrito carrito = carritoRepository.findByClienteId(cliente.getId_usuario())
            .orElseThrow(() -> new IllegalStateException("El carrito está vacío"));
        if (carrito.getItems().isEmpty()) {
            throw new IllegalStateException("El carrito está vacío");
        }

        // 3. Validar Domicilio y que sea uno de los domicilios guardados del cliente
        // (evita que un cliente arme un pedido con el domicilio de otro).
        Domicilio domicilio = domicilioRepository.findById(request.idDomicilio())
            .orElseThrow(() -> new EntityNotFoundException("Domicilio no encontrado"));

        boolean esDomicilioDelCliente = cliente.getDomicilio() != null
                && cliente.getDomicilio().stream().anyMatch(d -> d.getId().equals(domicilio.getId()));
        if (!esDomicilioDelCliente) {
            throw new AccessDeniedException("El domicilio indicado no pertenece al cliente");
        }

        // 4. Construir Pedido
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

        // Sucursal que factura el pedido. El negocio tiene un solo local (ver "Sobre el
        // proyecto" de la propuesta), así que alcanza con la primera activa; sin esto la
        // factura queda sin "Datos de la empresa" porque Factura los obtiene vía
        // pedido.sucursal.empresa.
        sucursalRepository.findByActivoTrue().stream().findFirst().ifPresent(pedido::setSucursal);

        // 5. Copiar los ítems del carrito al pedido, congelando el precio actual del
        // artículo en el subtotal (a diferencia del carrito, el pedido no debe cambiar
        // de precio después de confirmado).
        carrito.getItems().forEach(item -> {
            Articulo articulo = item.getArticulo();

            DetallePedido detalle = DetallePedido.builder()
                .articulo(articulo)
                .cantidad(item.getCantidad())
                .subtotal(articulo.getPrecioVenta().multiply(BigDecimal.valueOf(item.getCantidad())))
                .pedido(pedido)
                .build();

            pedido.getDetallePedidos().add(detalle);
        });

        // 6. Costo de envío: fijo por ciudad del domicilio elegido (ver Ciudad.costoEnvio),
        // no aplica si el pedido es para retirar por local.
        if (request.tipoEnvio() == TipoEnvio.DELIVERY) {
            BigDecimal costoEnvio = domicilio.getCiudad().getCostoEnvio();
            pedido.setGastosEnvio(costoEnvio != null ? costoEnvio : BigDecimal.ZERO);
        }

        // 7. Lógica de negocio de tu entidad
        pedido.calcularTotal();
        pedido.calcularCostoTotal();
        pedido.reservarStock();

        Pedido guardado = pedidoRepository.save(pedido);

        // 8. El carrito se vacía recién cuando el pedido quedó creado con éxito.
        carrito.getItems().clear();
        carritoRepository.save(carrito);

        return pedidoMapper.toFullResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoFullResponse buscarPedidoPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));
        return pedidoMapper.toFullResponse(pedido);
    }

    @Override
    @Transactional
    public PedidoFullResponse cambiarEstado(Long id, Estado nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));

        Estado estadoAnterior = pedido.getEstado();
        validarTransicion(estadoAnterior, nuevoEstado);

        if (nuevoEstado == Estado.ENTREGADO && pedido.getTipoEnvio() == TipoEnvio.DELIVERY) {
            throw new IllegalStateException(
                "Un pedido con envío a domicilio se entrega desde el flujo del repartidor (PATCH /pedidos/{id}/entregar), no desde este endpoint");
        }

        aplicarCambioEstado(pedido, nuevoEstado);

        return pedidoMapper.toFullResponse(pedidoRepository.save(pedido));
    }

    private void validarTransicion(Estado desde, Estado hacia) {
        if (!TRANSICIONES_VALIDAS.getOrDefault(desde, Set.of()).contains(hacia)) {
            throw new IllegalStateException("No se puede pasar de " + desde + " a " + hacia);
        }
    }

    // Efectos de negocio de cambiar el estado (stock, facturación), compartidos entre el
    // flujo genérico (cambiarEstado) y el flujo de repartidor (tomarPedido/marcarEntregado).
    private void aplicarCambioEstado(Pedido pedido, Estado nuevoEstado) {
        Estado estadoAnterior = pedido.getEstado();

        if (nuevoEstado == Estado.EN_PROCESO && estadoAnterior == Estado.PENDIENTE) {
            pedido.confirmarStock();
        }

        if (nuevoEstado == Estado.CANCELADO && estadoAnterior != Estado.CANCELADO) {
            pedido.liberarStock();
        }

        if (nuevoEstado == Estado.DEMORADO) {
            pedido.demorar();
        }

        pedido.setEstado(nuevoEstado);

        // Si el pedido se entrega y el pago ya fue confirmado (PAGADO), factura sola.
        if (nuevoEstado == Estado.ENTREGADO && pedido.getEstadoPago() == EstadoPago.PAGADO) {
            facturaService.generarFacturaDesdePedido(pedido.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoEntregaResponse> listarDisponiblesParaEnvio() {
        List<Pedido> pedidos = pedidoRepository.findDisponiblesParaEnvio(Estado.PREPARADO, TipoEnvio.DELIVERY);
        return pedidoMapper.toEntregaResponseList(pedidos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoEntregaResponse> misEntregas(Long repartidorId) {
        List<Pedido> pedidos = pedidoRepository.findByRepartidorIdOrderByFechaPedidoDesc(repartidorId);
        return pedidoMapper.toEntregaResponseList(pedidos);
    }

    @Override
    @Transactional
    public PedidoFullResponse tomarPedido(Long pedidoId, Long repartidorId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));

        if (pedido.getTipoEnvio() != TipoEnvio.DELIVERY) {
            throw new IllegalStateException("Este pedido no es de envío a domicilio");
        }
        if (pedido.getEstado() != Estado.PREPARADO) {
            throw new IllegalStateException("El pedido todavía no está listo para repartir (estado actual: " + pedido.getEstado() + ")");
        }
        if (pedido.getRepartidor() != null) {
            throw new IllegalStateException("El pedido ya fue tomado por otro repartidor");
        }

        Empleado repartidor = empleadoRepository.findById(repartidorId)
            .orElseThrow(() -> new EntityNotFoundException("Repartidor no encontrado"));

        pedido.setRepartidor(repartidor);
        aplicarCambioEstado(pedido, Estado.EN_CAMINO);

        return pedidoMapper.toFullResponse(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional
    public PedidoFullResponse marcarEntregado(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));

        if (pedido.getEstado() != Estado.EN_CAMINO) {
            throw new IllegalStateException("El pedido no está en camino (estado actual: " + pedido.getEstado() + ")");
        }

        aplicarCambioEstado(pedido, Estado.ENTREGADO);

        return pedidoMapper.toFullResponse(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoCocinaResponse> listarParaCocina() {
        // El cocinero recién ve el pedido una vez que el cajero lo aceptó (PENDIENTE -> EN_PROCESO);
        // PREPARADO ya salió de cocina y queda fuera de esta pantalla.
        List<Estado> estadosCocina = List.of(
            Estado.EN_PROCESO,
            Estado.DEMORADO
        );
        List<Pedido> pedidos = pedidoRepository.findByEstadoInOrderByHoraEstimadaFinalizacionAsc(estadosCocina);
        return pedidoMapper.toCocinaResponseList(pedidos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoBaseResponse> historialCliente(Long clienteId) {
        List<Pedido> pedidos = pedidoRepository.findByClienteIdOrderByFechaPedidoDesc(clienteId);
        return pedidoMapper.toBaseResponseList(pedidos);
    }

    @Override
    @Transactional
    public PreferenciaPagoResponse crearPreferenciaPago(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));

        if (pedido.getFormaPago() != FormaPago.MERCADO_PAGO) {
            throw new IllegalStateException("El pedido no está configurado con forma de pago Mercado Pago");
        }
        if (pedido.getEstadoPago() == EstadoPago.PAGADO) {
            throw new IllegalStateException("El pedido ya fue pagado");
        }

        PreferenceItemRequest item = PreferenceItemRequest.builder()
                .id(pedido.getId().toString())
                .title("Pedido #" + pedido.getId() + " - El Buen Sabor")
                .quantity(1)
                .currencyId("ARS")
                .unitPrice(pedido.getTotal())
                .build();

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(List.of(item))
                // Le permite al webhook encontrar de nuevo este Pedido a partir del pago de MP.
                .externalReference(pedido.getId().toString())
                .notificationUrl(notificationUrl)
                .backUrls(PreferenceBackUrlsRequest.builder()
                        .success(backUrlSuccess)
                        .pending(backUrlPending)
                        .failure(backUrlFailure)
                        .build())
                .autoReturn("approved")
                .build();

        Preference preference;
        try {
            preference = new PreferenceClient().create(preferenceRequest);
        } catch (MPApiException | MPException e) {
            throw new IllegalStateException("No se pudo crear la preferencia de pago en Mercado Pago: " + e.getMessage(), e);
        }

        DetalleMercadoPago detalle = pedido.getDetalleMercadoPago();
        if (detalle == null) {
            detalle = new DetalleMercadoPago();
        }
        detalle.setIdPreferencia(preference.getId());
        detalle.setId_Orden(pedido.getId().intValue());
        pedido.setDetalleMercadoPago(detalle);
        pedidoRepository.save(pedido);

        return new PreferenciaPagoResponse(preference.getId(), preference.getInitPoint(), preference.getSandboxInitPoint());
    }

    @Override
    @Transactional
    public void procesarNotificacionPago(Long paymentId) {
        Payment payment;
        try {
            payment = new PaymentClient().get(paymentId);
        } catch (MPApiException | MPException e) {
            throw new IllegalStateException("No se pudo consultar el pago " + paymentId + " en Mercado Pago: " + e.getMessage(), e);
        }

        if (payment.getExternalReference() == null) {
            throw new EntityNotFoundException("El pago " + paymentId + " no tiene external_reference (no se puede vincular a un pedido)");
        }

        Long pedidoId = Long.valueOf(payment.getExternalReference());
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado: " + pedidoId));

        pedido.setEstadoPago(mapearEstadoPago(payment.getStatus()));

        DetalleMercadoPago detalle = pedido.getDetalleMercadoPago();
        if (detalle == null) {
            detalle = new DetalleMercadoPago();
            detalle.setId_Orden(pedido.getId().intValue());
        }
        detalle.setId_Pago(payment.getId());
        detalle.setTipoPago(payment.getPaymentTypeId());
        pedido.setDetalleMercadoPago(detalle);

        pedidoRepository.save(pedido);
    }

    private EstadoPago mapearEstadoPago(String estadoMercadoPago) {
        if (estadoMercadoPago == null) {
            return EstadoPago.PENDIENTE;
        }
        return switch (estadoMercadoPago) {
            case "approved" -> EstadoPago.PAGADO;
            case "pending", "in_process", "authorized" -> EstadoPago.PENDIENTE;
            default -> EstadoPago.RECHAZADO; // rejected, cancelled, refunded, charged_back
        };
    }
}