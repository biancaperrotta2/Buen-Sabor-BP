package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.entity.Empleado;
import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.entity.Enums.EstadoPago;
import com.aluracursos.buensaborbp.entity.Enums.FormaPago;
import com.aluracursos.buensaborbp.entity.Enums.TipoEnvio;
import com.aluracursos.buensaborbp.entity.Pedido;
import com.aluracursos.buensaborbp.mapper.PedidoMapper;
import com.aluracursos.buensaborbp.repository.CarritoRepository;
import com.aluracursos.buensaborbp.repository.ClienteRepository;
import com.aluracursos.buensaborbp.repository.DomicilioRepository;
import com.aluracursos.buensaborbp.repository.EmpleadoRepository;
import com.aluracursos.buensaborbp.repository.FacturaRepository;
import com.aluracursos.buensaborbp.repository.PedidoRepository;
import com.aluracursos.buensaborbp.repository.SucursalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.time.OffsetDateTime;
import com.mercadopago.resources.payment.Payment;
import com.aluracursos.buensaborbp.dto.VerificacionPagoResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Cubre las reglas del "Pedido payment/invoice workflow" agregadas el 2026-09-21 (ver
// CLAUDE.md): bloquear aceptar un Mercado Pago no pagado, confirmar cobro en efectivo como
// paso separado, y facturar sola al quedar Entregado+Pagado sin importar el orden. Son
// unitarios con Mockito: sin Spring, sin base de datos, solo la clase con sus dependencias
// mockeadas.
@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private PedidoMapper pedidoMapper;
    @Mock private CarritoRepository carritoRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private DomicilioRepository domicilioRepository;
    @Mock private EmpleadoRepository empleadoRepository;
    @Mock private SucursalRepository sucursalRepository;
    @Mock private FacturaService facturaService;
    @Mock private FacturaRepository facturaRepository;
    @Mock private MercadoPagoPagos mercadoPagoPagos;
    @Mock private TiempoEstimadoPedido tiempoEstimadoPedido;

    private PedidoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PedidoServiceImpl(pedidoRepository, pedidoMapper, carritoRepository, clienteRepository,
                domicilioRepository, empleadoRepository, sucursalRepository, facturaService, facturaRepository,
                mercadoPagoPagos, tiempoEstimadoPedido);
        // save() se llama al final de casi todos los métodos; devolver el mismo objeto que
        // recibe alcanza para que el resto de la aserción (estado del Pedido en memoria) valga.
        lenient().when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Pedido pedido(Long id, Estado estado, EstadoPago estadoPago, FormaPago formaPago, TipoEnvio tipoEnvio) {
        return Pedido.builder()
                .id(id).estado(estado).estadoPago(estadoPago).formaPago(formaPago).tipoEnvio(tipoEnvio)
                .detallePedidos(java.util.List.of())
                .build();
    }

    @Nested
    @DisplayName("cambiarEstado")
    class CambiarEstado {

        @Test
        @DisplayName("Una transición fuera de la máquina de estados se rechaza")
        void transicionInvalida_lanzaExcepcion() {
            Pedido p = pedido(1L, Estado.PENDIENTE, EstadoPago.PENDIENTE, FormaPago.EFECTIVO, TipoEnvio.TAKE_AWAY);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> service.cambiarEstado(1L, Estado.PREPARADO));

            assertThat(ex.getMessage()).contains("PENDIENTE").contains("PREPARADO");
        }

        @Test
        @DisplayName("No se puede aceptar (PENDIENTE->EN_PROCESO) un Mercado Pago todavía no pagado")
        void aceptarMercadoPagoNoPagado_lanzaExcepcion() {
            Pedido p = pedido(1L, Estado.PENDIENTE, EstadoPago.PENDIENTE, FormaPago.MERCADO_PAGO, TipoEnvio.TAKE_AWAY);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));

            assertThrows(IllegalStateException.class, () -> service.cambiarEstado(1L, Estado.EN_PROCESO));
            verify(pedidoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Si el Mercado Pago ya está pagado, sí se puede aceptar y confirma el stock")
        void aceptarMercadoPagoPagado_permite() {
            Pedido p = pedido(1L, Estado.PENDIENTE, EstadoPago.PAGADO, FormaPago.MERCADO_PAGO, TipoEnvio.TAKE_AWAY);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));

            service.cambiarEstado(1L, Estado.EN_PROCESO);

            assertThat(p.getEstado()).isEqualTo(Estado.EN_PROCESO);
        }

        @Test
        @DisplayName("Un pedido en efectivo se puede aceptar sin importar el estado de pago")
        void aceptarEfectivo_permiteSinImportarPago() {
            Pedido p = pedido(1L, Estado.PENDIENTE, EstadoPago.PENDIENTE, FormaPago.EFECTIVO, TipoEnvio.TAKE_AWAY);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));

            service.cambiarEstado(1L, Estado.EN_PROCESO);

            assertThat(p.getEstado()).isEqualTo(Estado.EN_PROCESO);
        }

        @Test
        @DisplayName("Entregar un DELIVERY por el endpoint genérico se rechaza (es flujo del repartidor)")
        void entregarDeliveryPorEndpointGenerico_lanzaExcepcion() {
            Pedido p = pedido(1L, Estado.PREPARADO, EstadoPago.PAGADO, FormaPago.EFECTIVO, TipoEnvio.DELIVERY);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> service.cambiarEstado(1L, Estado.ENTREGADO));
            assertThat(ex.getMessage()).contains("repartidor");
        }

        @Test
        @DisplayName("Cancelar un pedido libera el stock reservado")
        void cancelar_liberaStock() {
            Pedido p = pedido(1L, Estado.PENDIENTE, EstadoPago.PENDIENTE, FormaPago.EFECTIVO, TipoEnvio.TAKE_AWAY);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));

            // liberarStock() sobre detallePedidos vacío no rompe nada; lo que importa acá es
            // que el estado termine en CANCELADO (el detalle de qué se libera ya está
            // cubierto por PedidoTest).
            service.cambiarEstado(1L, Estado.CANCELADO);

            assertThat(p.getEstado()).isEqualTo(Estado.CANCELADO);
        }

        @Test
        @DisplayName("Al entregar con el pago ya confirmado, factura sola")
        void entregarConPagoConfirmado_generaFactura() {
            Pedido p = pedido(1L, Estado.PREPARADO, EstadoPago.PAGADO, FormaPago.MERCADO_PAGO, TipoEnvio.TAKE_AWAY);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));
            when(facturaRepository.existsByPedidoId(1L)).thenReturn(false);

            service.cambiarEstado(1L, Estado.ENTREGADO);

            verify(facturaService).generarFacturaDesdePedido(1L);
        }

        @Test
        @DisplayName("Al entregar SIN el pago confirmado, no factura (efectivo sin cobrar todavía)")
        void entregarSinPago_noFactura() {
            Pedido p = pedido(1L, Estado.PREPARADO, EstadoPago.PENDIENTE, FormaPago.EFECTIVO, TipoEnvio.TAKE_AWAY);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));

            service.cambiarEstado(1L, Estado.ENTREGADO);

            verify(facturaService, never()).generarFacturaDesdePedido(anyLong());
        }

        @Test
        @DisplayName("Si ya existe una factura para el pedido, no la genera de nuevo")
        void yaFacturado_noFacturaDeNuevo() {
            Pedido p = pedido(1L, Estado.PREPARADO, EstadoPago.PAGADO, FormaPago.MERCADO_PAGO, TipoEnvio.TAKE_AWAY);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));
            when(facturaRepository.existsByPedidoId(1L)).thenReturn(true);

            service.cambiarEstado(1L, Estado.ENTREGADO);

            verify(facturaService, never()).generarFacturaDesdePedido(anyLong());
        }

        @Test
        @DisplayName("Pedido inexistente: EntityNotFoundException")
        void pedidoInexistente_lanzaExcepcion() {
            when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> service.cambiarEstado(99L, Estado.EN_PROCESO));
        }
    }

    @Nested
    @DisplayName("confirmarCobroEfectivo")
    class ConfirmarCobroEfectivo {

        @Test
        @DisplayName("Un pedido de Mercado Pago se rechaza (su pago lo confirma el webhook, no este endpoint)")
        void pedidoMercadoPago_lanzaExcepcion() {
            Pedido p = pedido(1L, Estado.PREPARADO, EstadoPago.PENDIENTE, FormaPago.MERCADO_PAGO, TipoEnvio.TAKE_AWAY);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> service.confirmarCobroEfectivo(1L));
            assertThat(ex.getMessage()).contains("Mercado Pago");
        }

        @Test
        @DisplayName("Un pedido ya cancelado se rechaza")
        void pedidoCancelado_lanzaExcepcion() {
            Pedido p = pedido(1L, Estado.CANCELADO, EstadoPago.PENDIENTE, FormaPago.EFECTIVO, TipoEnvio.TAKE_AWAY);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));

            assertThrows(IllegalStateException.class, () -> service.confirmarCobroEfectivo(1L));
        }

        @Test
        @DisplayName("Un pedido ya cobrado no se puede volver a cobrar")
        void yaCobrado_lanzaExcepcion() {
            Pedido p = pedido(1L, Estado.PREPARADO, EstadoPago.PAGADO, FormaPago.EFECTIVO, TipoEnvio.TAKE_AWAY);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));

            assertThrows(IllegalStateException.class, () -> service.confirmarCobroEfectivo(1L));
        }

        @Test
        @DisplayName("Marca PAGADO; si el pedido ya estaba Entregado, factura en el mismo paso")
        void marcaPagado_facturaSiYaEntregado() {
            Pedido p = pedido(1L, Estado.ENTREGADO, EstadoPago.PENDIENTE, FormaPago.EFECTIVO, TipoEnvio.TAKE_AWAY);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));
            when(facturaRepository.existsByPedidoId(1L)).thenReturn(false);

            service.confirmarCobroEfectivo(1L);

            assertThat(p.getEstadoPago()).isEqualTo(EstadoPago.PAGADO);
            verify(facturaService).generarFacturaDesdePedido(1L);
        }

        @Test
        @DisplayName("Marca PAGADO sin facturar todavía si el pedido no fue entregado aún")
        void marcaPagado_sinFacturarSiNoEntregado() {
            Pedido p = pedido(1L, Estado.PREPARADO, EstadoPago.PENDIENTE, FormaPago.EFECTIVO, TipoEnvio.TAKE_AWAY);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));

            service.confirmarCobroEfectivo(1L);

            assertThat(p.getEstadoPago()).isEqualTo(EstadoPago.PAGADO);
            verify(facturaService, never()).generarFacturaDesdePedido(anyLong());
        }
    }

    @Nested
    @DisplayName("tomarPedido (repartidor)")
    class TomarPedido {

        @Test
        @DisplayName("No se puede tomar un pedido que no es de delivery")
        void noEsDelivery_lanzaExcepcion() {
            Pedido p = pedido(1L, Estado.PREPARADO, EstadoPago.PAGADO, FormaPago.EFECTIVO, TipoEnvio.TAKE_AWAY);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));

            assertThrows(IllegalStateException.class, () -> service.tomarPedido(1L, 5L));
        }

        @Test
        @DisplayName("No se puede tomar un pedido que todavía no está PREPARADO")
        void noEstaPreparado_lanzaExcepcion() {
            Pedido p = pedido(1L, Estado.EN_PROCESO, EstadoPago.PAGADO, FormaPago.EFECTIVO, TipoEnvio.DELIVERY);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));

            assertThrows(IllegalStateException.class, () -> service.tomarPedido(1L, 5L));
        }

        @Test
        @DisplayName("Un pedido ya tomado por otro repartidor no se puede volver a tomar")
        void yaTomado_lanzaExcepcion() {
            Pedido p = pedido(1L, Estado.PREPARADO, EstadoPago.PAGADO, FormaPago.EFECTIVO, TipoEnvio.DELIVERY);
            p.setRepartidor(Empleado.builder().build());
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));

            assertThrows(IllegalStateException.class, () -> service.tomarPedido(1L, 5L));
        }

        @Test
        @DisplayName("Asigna el repartidor y pasa a EN_CAMINO")
        void tomaElPedido() {
            Pedido p = pedido(1L, Estado.PREPARADO, EstadoPago.PAGADO, FormaPago.EFECTIVO, TipoEnvio.DELIVERY);
            Empleado repartidor = Empleado.builder().build();
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));
            when(empleadoRepository.findById(5L)).thenReturn(Optional.of(repartidor));

            service.tomarPedido(1L, 5L);

            assertThat(p.getRepartidor()).isSameAs(repartidor);
            assertThat(p.getEstado()).isEqualTo(Estado.EN_CAMINO);
        }
    }

    @Nested
    @DisplayName("marcarEntregado (repartidor)")
    class MarcarEntregado {

        @Test
        @DisplayName("No se puede entregar un pedido que no está EN_CAMINO")
        void noEstaEnCamino_lanzaExcepcion() {
            Pedido p = pedido(1L, Estado.PREPARADO, EstadoPago.PAGADO, FormaPago.EFECTIVO, TipoEnvio.DELIVERY);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));

            assertThrows(IllegalStateException.class, () -> service.marcarEntregado(1L));
        }

        @Test
        @DisplayName("Un delivery de Mercado Pago ya pagado factura solo al entregarse")
        void entregaConMercadoPagoPagado_facturaSola() {
            Pedido p = pedido(1L, Estado.EN_CAMINO, EstadoPago.PAGADO, FormaPago.MERCADO_PAGO, TipoEnvio.DELIVERY);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));
            when(facturaRepository.existsByPedidoId(1L)).thenReturn(false);

            service.marcarEntregado(1L);

            assertThat(p.getEstado()).isEqualTo(Estado.ENTREGADO);
            verify(facturaService).generarFacturaDesdePedido(1L);
        }
    }

    // verificarPago: el sistema le pregunta a Mercado Pago en vez de esperar el webhook (que
    // depende de que MP pueda llegar al servidor). Usa la misma lógica que el webhook.
    @Nested
    @DisplayName("verificarPago")
    class VerificarPago {
        private Payment pago(String status, OffsetDateTime fecha) {
            Payment p = org.mockito.Mockito.mock(Payment.class);
            lenient().when(p.getStatus()).thenReturn(status);
            lenient().when(p.getDateCreated()).thenReturn(fecha);
            lenient().when(p.getId()).thenReturn(99L);
            return p;
        }

        @Test
        @DisplayName("Con un pago aprobado en MP, el pedido pasa a PAGADO")
        void aprobado_marcaPagado() {
            Pedido p = pedido(1L, Estado.PENDIENTE, EstadoPago.PENDIENTE, FormaPago.MERCADO_PAGO, TipoEnvio.TAKE_AWAY);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));
            Payment aprobado = pago("approved", OffsetDateTime.now());
            when(mercadoPagoPagos.buscarPorPedido(1L)).thenReturn(List.of(aprobado));

            VerificacionPagoResponse r = service.verificarPago(1L);

            assertThat(r.pagoEncontrado()).isTrue();
            assertThat(r.estadoPago()).isEqualTo(EstadoPago.PAGADO);
            assertThat(p.getEstadoPago()).isEqualTo(EstadoPago.PAGADO);
        }

        @Test
        @DisplayName("Con un pago rechazado, el pedido se cancela (misma regla que el webhook)")
        void rechazado_cancelaPedido() {
            Pedido p = pedido(1L, Estado.PENDIENTE, EstadoPago.PENDIENTE, FormaPago.MERCADO_PAGO, TipoEnvio.TAKE_AWAY);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));
            Payment rechazado = pago("rejected", OffsetDateTime.now());
            when(mercadoPagoPagos.buscarPorPedido(1L)).thenReturn(List.of(rechazado));

            service.verificarPago(1L);

            assertThat(p.getEstadoPago()).isEqualTo(EstadoPago.RECHAZADO);
            assertThat(p.getEstado()).isEqualTo(Estado.CANCELADO);
        }

        @Test
        @DisplayName("Si hay un rechazado y uno aprobado, gana el aprobado")
        void rechazadoYAprobado_ganaAprobado() {
            Pedido p = pedido(1L, Estado.PENDIENTE, EstadoPago.PENDIENTE, FormaPago.MERCADO_PAGO, TipoEnvio.TAKE_AWAY);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));
            Payment rechazado = pago("rejected", OffsetDateTime.now());
            Payment aprobado = pago("approved", OffsetDateTime.now().minusMinutes(5));
            when(mercadoPagoPagos.buscarPorPedido(1L)).thenReturn(List.of(rechazado, aprobado));

            service.verificarPago(1L);

            assertThat(p.getEstadoPago()).isEqualTo(EstadoPago.PAGADO);
            assertThat(p.getEstado()).isEqualTo(Estado.PENDIENTE);
        }

        @Test
        @DisplayName("Sin pagos en MP, no cambia nada y avisa que no encontró pago")
        void sinPagos_noCambiaNada() {
            Pedido p = pedido(1L, Estado.PENDIENTE, EstadoPago.PENDIENTE, FormaPago.MERCADO_PAGO, TipoEnvio.TAKE_AWAY);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));
            when(mercadoPagoPagos.buscarPorPedido(1L)).thenReturn(List.of());

            VerificacionPagoResponse r = service.verificarPago(1L);

            assertThat(r.pagoEncontrado()).isFalse();
            assertThat(p.getEstadoPago()).isEqualTo(EstadoPago.PENDIENTE);
            verify(pedidoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Un intento rechazado posterior no deshace un pago ya aprobado (webhook)")
        void rechazadoDespuesDeAprobado_noCancela() {
            Pedido p = pedido(1L, Estado.PENDIENTE, EstadoPago.PAGADO, FormaPago.MERCADO_PAGO, TipoEnvio.TAKE_AWAY);
            com.aluracursos.buensaborbp.entity.DetalleMercadoPago d = new com.aluracursos.buensaborbp.entity.DetalleMercadoPago();
            d.setId_Pago(10L);
            p.setDetalleMercadoPago(d);
            Payment rechazado = pago("rejected", OffsetDateTime.now());
            lenient().when(rechazado.getId()).thenReturn(11L);
            when(rechazado.getExternalReference()).thenReturn("1");
            when(mercadoPagoPagos.obtener(11L)).thenReturn(rechazado);
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));

            service.procesarNotificacionPago(11L);

            assertThat(p.getEstadoPago()).isEqualTo(EstadoPago.PAGADO);
            assertThat(p.getEstado()).isEqualTo(Estado.PENDIENTE);
        }

        @Test
        @DisplayName("Un pedido en efectivo no se verifica en Mercado Pago")
        void efectivo_rechaza() {
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(
                    pedido(1L, Estado.PENDIENTE, EstadoPago.PENDIENTE, FormaPago.EFECTIVO, TipoEnvio.TAKE_AWAY)));

            assertThrows(IllegalStateException.class, () -> service.verificarPago(1L));
            org.mockito.Mockito.verifyNoInteractions(mercadoPagoPagos);
        }
    }
}
