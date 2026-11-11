package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.PedidoBaseResponse;
import com.aluracursos.buensaborbp.dto.VerificacionPagoResponse;
import com.aluracursos.buensaborbp.dto.PedidoCajaResponse;
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
import com.aluracursos.buensaborbp.repository.FacturaRepository;
import com.aluracursos.buensaborbp.repository.PedidoRepository;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferenceRequest;
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
    private final FacturaRepository facturaRepository;
    private final MercadoPagoPagos mercadoPagoPagos;
    private final TiempoEstimadoPedido tiempoEstimadoPedido;

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
                             FacturaService facturaService,
                             FacturaRepository facturaRepository,
                             MercadoPagoPagos mercadoPagoPagos,
                             TiempoEstimadoPedido tiempoEstimadoPedido) {
        super(pedidoRepository);
        this.pedidoRepository = pedidoRepository;
        this.pedidoMapper = pedidoMapper;
        this.carritoRepository = carritoRepository;
        this.clienteRepository = clienteRepository;
        this.domicilioRepository = domicilioRepository;
        this.empleadoRepository = empleadoRepository;
        this.sucursalRepository = sucursalRepository;
        this.facturaService = facturaService;
        this.facturaRepository = facturaRepository;
        this.mercadoPagoPagos = mercadoPagoPagos;
        this.tiempoEstimadoPedido = tiempoEstimadoPedido;
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

        // 3. Para DELIVERY, validar que el domicilio exista y sea uno de los domicilios
        // guardados del cliente (evita que arme un pedido con el domicilio de otro). Para
        // TAKE_AWAY no hace falta domicilio: el cliente retira por el local.
        Domicilio domicilio = null;
        String domicilioSnapshot = "Retiro en el local";
        String aclaracionDomicilio = textoOpcional(request.aclaracionDomicilio());
        if (request.tipoEnvio() == TipoEnvio.DELIVERY) {
            if (request.idDomicilio() == null) {
                throw new IllegalArgumentException("El domicilio es obligatorio para un envío a domicilio");
            }
            Domicilio domicilioEncontrado = domicilioRepository.findById(request.idDomicilio())
                .orElseThrow(() -> new EntityNotFoundException("Domicilio no encontrado"));

            boolean esDomicilioDelCliente = cliente.getDomicilio() != null
                    && cliente.getDomicilio().stream().anyMatch(d -> d.getId().equals(domicilioEncontrado.getId()));
            if (!esDomicilioDelCliente) {
                throw new AccessDeniedException("El domicilio indicado no pertenece al cliente");
            }
            domicilio = domicilioEncontrado;
            domicilioSnapshot = domicilio.getCalle() + " " + domicilio.getNumero();
            // Si el cliente no escribió una aclaración puntual, va la guardada en la dirección.
            if (aclaracionDomicilio == null) {
                aclaracionDomicilio = textoOpcional(domicilio.getAclaraciones());
            }
        }

        validarHorarioDeAtencion();

        // 4. Construir Pedido
        Pedido pedido = Pedido.builder()
            .fechaPedido(LocalDate.now())
            .estado(Estado.PENDIENTE)
            .estadoPago(EstadoPago.PENDIENTE) // Por defecto pendiente hasta que MP confirme
            .tipoEnvio(request.tipoEnvio())
            .formaPago(request.formaPago())
            .aclaracionDomicilio(aclaracionDomicilio)
            .aclaraciones(textoOpcional(request.aclaraciones()))
            .cliente(cliente)
            .domicilio(domicilio)
            .domicilioSnapshot(domicilioSnapshot)
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
        pedido.setHoraEstimadaFinalizacion(tiempoEstimadoPedido.calcular(pedido, LocalTime.now()));

        Pedido guardado = pedidoRepository.save(pedido);

        // 8. El carrito se vacía recién cuando el pedido quedó creado con éxito.
        carrito.getItems().clear();
        carritoRepository.save(carrito);

        return pedidoMapper.toFullResponse(guardado);
    }

    // Si no hay ninguna sucursal cargada todavía, no bloquea la creación de pedidos por esto
    // (ese es un problema de datos, no de horario) — solo valida contra la que exista.
    private void validarHorarioDeAtencion() {
        sucursalRepository.findByActivoTrue().stream().findFirst().ifPresent(sucursal -> {
            LocalTime ahora = LocalTime.now();
            if (!estaEnHorario(sucursal.getHorarioApertura(), sucursal.getHorarioCierre(), ahora)) {
                throw new IllegalStateException("El local está cerrado. Horario de atención: "
                        + sucursal.getHorarioApertura() + " a " + sucursal.getHorarioCierre());
            }
        });
    }

    // Contempla el caso de un horario que cruza la medianoche (ej. 18:00 a 00:00).
    private boolean estaEnHorario(LocalTime apertura, LocalTime cierre, LocalTime ahora) {
        if (apertura.equals(cierre)) {
            return true; // abierto las 24 horas
        }
        if (apertura.isBefore(cierre)) {
            return !ahora.isBefore(apertura) && ahora.isBefore(cierre);
        }
        return !ahora.isBefore(apertura) || ahora.isBefore(cierre);
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

        // No tiene sentido empezar a cocinar (ni cobrar dos veces) un pedido de Mercado Pago
        // que el comprador todavía no terminó de pagar.
        if (nuevoEstado == Estado.EN_PROCESO && pedido.getFormaPago() == FormaPago.MERCADO_PAGO
                && pedido.getEstadoPago() != EstadoPago.PAGADO) {
            throw new IllegalStateException(
                "Este pedido se paga con Mercado Pago y todavía no está confirmado el pago; no se puede aceptar.");
        }

        // Cancelar así no devolvería la plata: un pedido ya cobrado por Mercado Pago se cancela
        // solo con "Rechazar", que hace el reintegro.
        if (nuevoEstado == Estado.CANCELADO && pedido.getFormaPago() == FormaPago.MERCADO_PAGO
                && pedido.getEstadoPago() == EstadoPago.PAGADO) {
            throw new IllegalStateException(
                "Este pedido ya está pagado con Mercado Pago; cancelalo con Rechazar para que se reintegre el pago.");
        }

        aplicarCambioEstado(pedido, nuevoEstado);

        return pedidoMapper.toFullResponse(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional
    public PedidoFullResponse rechazar(Long id, String motivo) {
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));

        // El cajero rechaza al revisar la orden recién ingresada (enunciado: "Pendiente" -> "Cancelado").
        if (pedido.getEstado() != Estado.PENDIENTE) {
            throw new IllegalStateException("Solo se puede rechazar un pedido pendiente; el #" + id + " está " + pedido.getEstado());
        }
        String motivoLimpio = textoOpcional(motivo);
        if (motivoLimpio == null) {
            throw new IllegalArgumentException("Escribí el motivo del rechazo");
        }

        // Primero el reintegro: si Mercado Pago falla, el pedido sigue pendiente y no queda
        // cancelado como si se hubiera devuelto la plata.
        if (pedido.getFormaPago() == FormaPago.MERCADO_PAGO && pedido.getEstadoPago() == EstadoPago.PAGADO) {
            reintegrarPago(pedido);
        }

        pedido.setMotivoCancelacion(motivoLimpio);
        aplicarCambioEstado(pedido, Estado.CANCELADO);

        return pedidoMapper.toFullResponse(pedidoRepository.save(pedido));
    }

    private void reintegrarPago(Pedido pedido) {
        DetalleMercadoPago detalle = pedido.getDetalleMercadoPago();
        if (detalle == null || detalle.getId_Pago() == null) {
            throw new IllegalStateException("No se encontró el pago de Mercado Pago del pedido #" + pedido.getId()
                + " para reintegrarlo; probá \"Verificar pago\" primero.");
        }
        mercadoPagoPagos.reintegrar(detalle.getId_Pago());
        pedido.setEstadoPago(EstadoPago.REINTEGRADO);
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
            // Se recalcula al aceptar: el pedido pudo estar un rato pendiente y la cocina
            // pudo cargarse o liberarse mientras tanto.
            pedido.setHoraEstimadaFinalizacion(tiempoEstimadoPedido.calcular(pedido, LocalTime.now()));
        }

        if (nuevoEstado == Estado.CANCELADO && estadoAnterior != Estado.CANCELADO) {
            pedido.liberarStock();
        }

        if (nuevoEstado == Estado.DEMORADO) {
            pedido.demorar();
        }

        pedido.setEstado(nuevoEstado);

        facturarSiCorresponde(pedido);
    }

    // Entregado + Pagado, en cualquier orden, factura sola: con Mercado Pago normalmente ya
    // está PAGADO antes de llegar a ENTREGADO; con efectivo es al revés, se entrega primero y
    // el cobro se confirma después (ver confirmarCobroEfectivo). El chequeo de existsByPedidoId
    // evita reintentar si ya se facturó por el otro camino.
    private void facturarSiCorresponde(Pedido pedido) {
        if (pedido.getEstado() == Estado.ENTREGADO
                && pedido.getEstadoPago() == EstadoPago.PAGADO
                && !facturaRepository.existsByPedidoId(pedido.getId())) {
            facturaService.generarFacturaDesdePedido(pedido.getId());
        }
    }

    @Override
    @Transactional
    public PedidoFullResponse confirmarCobroEfectivo(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));

        if (pedido.getFormaPago() != FormaPago.EFECTIVO) {
            throw new IllegalStateException("Este pedido no se paga en efectivo; su pago lo confirma Mercado Pago.");
        }
        if (pedido.getEstado() == Estado.CANCELADO) {
            throw new IllegalStateException("El pedido está cancelado.");
        }
        if (pedido.getEstadoPago() == EstadoPago.PAGADO) {
            throw new IllegalStateException("El pedido ya fue marcado como cobrado.");
        }

        pedido.setEstadoPago(EstadoPago.PAGADO);
        facturarSiCorresponde(pedido);

        return pedidoMapper.toFullResponse(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoEntregaResponse> listarDisponiblesParaEnvio() {
        List<Pedido> pedidos = pedidoRepository.findDisponiblesParaEnvio(Estado.PREPARADO, TipoEnvio.DELIVERY);
        return pedidoMapper.toEntregaResponseList(pedidos);
    }

    // Solo las entregas EN_CAMINO: esta lista alimenta la pantalla operativa del
    // repartidor ("lo que tengo que entregar ahora"), no un historial. Sin este filtro,
    // un pedido ya entregado o cancelado se quedaba para siempre en la lista.
    @Override
    @Transactional(readOnly = true)
    public List<PedidoEntregaResponse> misEntregas(Long repartidorId) {
        List<Pedido> pedidos = pedidoRepository.findByRepartidorIdOrderByFechaPedidoDesc(repartidorId).stream()
                .filter(pedido -> pedido.getEstado() == Estado.EN_CAMINO)
                .toList();
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
    public List<PedidoCajaResponse> listarPendientes() {
        List<Pedido> pedidos = pedidoRepository.findByEstadoInOrderByHoraEstimadaFinalizacionAsc(List.of(Estado.PENDIENTE));
        return pedidoMapper.toCajaResponseList(pedidos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoCajaResponse> listarParaEntregarEnLocal() {
        List<Pedido> pedidos = pedidoRepository.findByEstadoAndTipoEnvioOrderByHoraEstimadaFinalizacionAsc(
                Estado.PREPARADO, TipoEnvio.TAKE_AWAY);
        return pedidoMapper.toCajaResponseList(pedidos);
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
                // Sin datos del comprador, el motor de revision de Mercado Pago puede no
                // tener con que validar el pago y dejar el checkout trabado.
                .payer(PreferencePayerRequest.builder()
                        .email(pedido.getCliente().getEmail())
                        .name(pedido.getCliente().getNombre())
                        .surname(pedido.getCliente().getApellido())
                        .build())
                // Le permite al webhook encontrar de nuevo este Pedido a partir del pago de MP.
                .externalReference(pedido.getId().toString())
                // Sin notification_url a propósito: los avisos llegan por los Webhooks configurados
                // en el panel de la aplicación de MP (firmados con x-signature). Con notification_url
                // MP además manda avisos IPN viejos (?topic=...) firmados distinto, que el webhook
                // rechaza y MP reintenta sin fin. La URL del webhook se configura solo en el panel.
                .backUrls(PreferenceBackUrlsRequest.builder()
                        .success(backUrlSuccess)
                        .pending(backUrlPending)
                        .failure(backUrlFailure)
                        .build())
                // auto_return exige que back_urls.success sea una URL publica (https, no
                // localhost); en desarrollo el frontend corre en http://localhost, asi que
                // Mercado Pago lo rechaza. Sin auto_return el pago y el webhook funcionan
                // igual, el comprador solo tiene que hacer clic en "Volver al sitio" en vez
                // de que lo redirija solo. Reactivar cuando el front tenga una URL https real.
                .build();

        Preference preference;
        try {
            preference = new PreferenceClient().create(preferenceRequest);
        } catch (MPApiException e) {
            throw new IllegalStateException("No se pudo crear la preferencia de pago en Mercado Pago: "
                    + e.getApiResponse().getContent(), e);
        } catch (MPException e) {
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
        Payment payment = mercadoPagoPagos.obtener(paymentId);

        if (payment.getExternalReference() == null) {
            throw new EntityNotFoundException("El pago " + paymentId + " no tiene external_reference (no se puede vincular a un pedido)");
        }

        Long pedidoId = Long.valueOf(payment.getExternalReference());
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado: " + pedidoId));

        aplicarPago(pedido, payment);
    }

    // En vez de esperar el webhook (que depende de que MP pueda llegar a este servidor), le
    // pregunta a MP por los pagos del pedido. Lo usan la pantalla de vuelta del checkout y el
    // botón "Verificar pago" de Caja. Si hay un pago aprobado se usa ese; si no, el más reciente.
    @Override
    @Transactional
    public VerificacionPagoResponse verificarPago(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado: " + pedidoId));
        if (pedido.getFormaPago() != FormaPago.MERCADO_PAGO) {
            throw new IllegalStateException("El pedido #" + pedidoId + " no se paga con Mercado Pago");
        }

        List<Payment> pagos = mercadoPagoPagos.buscarPorPedido(pedidoId);
        Payment elegido = pagos.stream()
                .filter(p -> "approved".equals(p.getStatus()))
                .findFirst()
                .orElseGet(() -> pagos.stream()
                        .filter(p -> p.getDateCreated() != null)
                        .max(java.util.Comparator.comparing(Payment::getDateCreated))
                        .orElse(pagos.isEmpty() ? null : pagos.get(0)));

        if (elegido == null) {
            return new VerificacionPagoResponse(pedidoId, pedido.getEstadoPago(), false);
        }
        aplicarPago(pedido, elegido);
        return new VerificacionPagoResponse(pedidoId, pedido.getEstadoPago(), true);
    }

    // Lógica común al webhook y a verificarPago: actualiza el estado de pago del pedido según
    // lo que dice MP. Idempotente: aplicar dos veces el mismo pago no cambia nada.
    private void aplicarPago(Pedido pedido, Payment payment) {
        // Ya se devolvió la plata: es un estado final, un aviso viejo de "approved" que llegue
        // tarde no lo puede volver a PAGADO.
        if (pedido.getEstadoPago() == EstadoPago.REINTEGRADO) {
            return;
        }

        // Un pedido ya pagado solo lo puede cambiar el MISMO pago que lo aprobó (ej. si después se
        // devuelve). El cliente puede intentar pagar varias veces la misma preferencia: un intento
        // rechazado que llega después de uno aprobado no debe deshacerlo ni cancelar el pedido
        // (pasó con el pedido #55 de prueba: aprobado 18:56, rechazado 18:57 → quedó cancelado).
        DetalleMercadoPago registrado = pedido.getDetalleMercadoPago();
        if (pedido.getEstadoPago() == EstadoPago.PAGADO
                && registrado != null && registrado.getId_Pago() != null
                && !registrado.getId_Pago().equals(payment.getId())) {
            return;
        }

        pedido.setEstadoPago(mapearEstadoPago(payment.getStatus()));

        DetalleMercadoPago detalle = pedido.getDetalleMercadoPago();
        if (detalle == null) {
            detalle = new DetalleMercadoPago();
            detalle.setId_Orden(pedido.getId().intValue());
        }
        detalle.setId_Pago(payment.getId());
        detalle.setTipoPago(payment.getPaymentTypeId());
        pedido.setDetalleMercadoPago(detalle);

        // El cajero rechazó el pedido mientras el cliente todavía tenía abierto el checkout y
        // después pagó igual: se le devuelve la plata en el momento.
        if (pedido.getEstadoPago() == EstadoPago.PAGADO && pedido.getEstado() == Estado.CANCELADO) {
            reintegrarPago(pedido);
        }

        // Un pago rechazado/cancelado/reembolsado nunca va a llegar a pagarse por este pedido
        // (el cajero ya tiene prohibido aceptar un Mercado Pago no pagado, así que quedaría
        // trabado para siempre con el stock reservado si no lo cancelamos acá). Se ignora si
        // el pedido ya está en un estado final, para que un reintento del webhook sea inofensivo.
        if ((pedido.getEstadoPago() == EstadoPago.RECHAZADO || pedido.getEstadoPago() == EstadoPago.REINTEGRADO)
                && pedido.getEstado() != Estado.CANCELADO
                && pedido.getEstado() != Estado.ENTREGADO) {
            aplicarCambioEstado(pedido, Estado.CANCELADO);
        }

        pedidoRepository.save(pedido);
    }

    private EstadoPago mapearEstadoPago(String estadoMercadoPago) {
        if (estadoMercadoPago == null) {
            return EstadoPago.PENDIENTE;
        }
        return switch (estadoMercadoPago) {
            case "approved" -> EstadoPago.PAGADO;
            case "pending", "in_process", "authorized" -> EstadoPago.PENDIENTE;
            case "refunded" -> EstadoPago.REINTEGRADO;
            default -> EstadoPago.RECHAZADO; // rejected, cancelled, charged_back
        };
    }

    private static String textoOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
