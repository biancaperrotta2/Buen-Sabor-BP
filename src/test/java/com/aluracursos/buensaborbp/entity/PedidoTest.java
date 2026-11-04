package com.aluracursos.buensaborbp.entity;

import com.aluracursos.buensaborbp.entity.Enums.TipoEnvio;
import com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum;
import com.aluracursos.buensaborbp.exception.StockInsuficienteException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Pedido.calcularTotal() es la fórmula que ve el cliente en el carrito y la que factura el
// negocio; un error acá afecta cada pedido. Corregido en la sesión del 2026-09-23 (el carrito
// mostraba un envío fijo de $1000 en vez de este cálculo real) — ver CLAUDE.md.
class PedidoTest {

    private static ArticuloInsumo insumo(String nombre, double precioVenta, double stockActual, double stockMaximo) {
        return ArticuloInsumo.builder()
                .nombre(nombre)
                .precioVenta(BigDecimal.valueOf(precioVenta))
                .precioCosto(BigDecimal.valueOf(precioVenta / 2))
                .unidadMedidaEnum(UnidadMedidaEnum.UNIDAD)
                .stockActual(stockActual)
                .stockMaximo(stockMaximo)
                .stockMinimo(1.0)
                .stockReservado(0.0)
                .build();
    }

    private static DetallePedido detalle(Pedido pedido, Articulo articulo, int cantidad) {
        DetallePedido detalle = DetallePedido.builder()
                .pedido(pedido)
                .articulo(articulo)
                .cantidad(cantidad)
                .subtotal(articulo.getPrecioVenta().multiply(BigDecimal.valueOf(cantidad)))
                .build();
        pedido.getDetallePedidos().add(detalle);
        return detalle;
    }

    private static Pedido pedidoConDetalles(TipoEnvio tipoEnvio) {
        return Pedido.builder().tipoEnvio(tipoEnvio).detallePedidos(new ArrayList<>()).build();
    }

    @Nested
    @DisplayName("calcularTotal")
    class CalcularTotal {

        @Test
        @DisplayName("TAKE_AWAY: descuenta el 10% del subtotal de productos")
        void takeAway_aplicaDescuentoDiezPorciento() {
            Pedido pedido = pedidoConDetalles(TipoEnvio.TAKE_AWAY);
            detalle(pedido, insumo("Pizza", 10000, 5, 20), 1);

            pedido.calcularTotal();

            assertThat(pedido.getDescuento()).isEqualByComparingTo("1000");
            assertThat(pedido.getTotal()).isEqualByComparingTo("9000");
        }

        @Test
        @DisplayName("DELIVERY: suma el costo de envío al subtotal, sin descuento")
        void delivery_sumaGastosEnvio() {
            Pedido pedido = pedidoConDetalles(TipoEnvio.DELIVERY);
            pedido.setGastosEnvio(BigDecimal.valueOf(2000));
            detalle(pedido, insumo("Pizza", 10000, 5, 20), 1);

            pedido.calcularTotal();

            assertThat(pedido.getDescuento()).isNull();
            assertThat(pedido.getTotal()).isEqualByComparingTo("12000");
        }

        @Test
        @DisplayName("DELIVERY sin gastosEnvio cargado todavía: no suma nada de envío")
        void delivery_sinGastosEnvio_noSumaNada() {
            Pedido pedido = pedidoConDetalles(TipoEnvio.DELIVERY);
            detalle(pedido, insumo("Pizza", 10000, 5, 20), 1);

            pedido.calcularTotal();

            assertThat(pedido.getTotal()).isEqualByComparingTo("10000");
        }

        @Test
        @DisplayName("Varios ítems: suma todos los subtotales antes de aplicar descuento/envío")
        void variosItems_sumaSubtotales() {
            Pedido pedido = pedidoConDetalles(TipoEnvio.TAKE_AWAY);
            detalle(pedido, insumo("Pizza", 10000, 5, 20), 2);
            detalle(pedido, insumo("Bebida", 2000, 5, 20), 3);

            pedido.calcularTotal();

            // subtotal = 20000 + 6000 = 26000; descuento 10% = 2600; total = 23400
            assertThat(pedido.getDescuento()).isEqualByComparingTo("2600");
            assertThat(pedido.getTotal()).isEqualByComparingTo("23400");
        }

        @Test
        @DisplayName("Sin detalles: total queda en cero, no explota")
        void sinDetalles_totalCero() {
            Pedido pedido = Pedido.builder().tipoEnvio(TipoEnvio.TAKE_AWAY).detallePedidos(List.of()).build();

            pedido.calcularTotal();

            assertThat(pedido.getTotal()).isEqualByComparingTo("0");
        }
    }

    @Nested
    @DisplayName("demorar")
    class Demorar {
        @Test
        @DisplayName("Pospone la hora estimada 10 minutos")
        void posponeDiezMinutos() {
            Pedido pedido = Pedido.builder().horaEstimadaFinalizacion(LocalTime.of(20, 0)).build();

            pedido.demorar();

            assertThat(pedido.getHoraEstimadaFinalizacion()).isEqualTo(LocalTime.of(20, 10));
        }

        @Test
        @DisplayName("Llamado dos veces, acumula (20 minutos en total)")
        void llamadoDosVeces_acumula() {
            Pedido pedido = Pedido.builder().horaEstimadaFinalizacion(LocalTime.of(20, 0)).build();

            pedido.demorar();
            pedido.demorar();

            assertThat(pedido.getHoraEstimadaFinalizacion()).isEqualTo(LocalTime.of(20, 20));
        }

        @Test
        @DisplayName("Sin hora estimada cargada, no explota y no hace nada")
        void sinHoraCargada_noHaceNada() {
            Pedido pedido = Pedido.builder().build();

            pedido.demorar();

            assertThat(pedido.getHoraEstimadaFinalizacion()).isNull();
        }
    }

    @Nested
    @DisplayName("stock (reservar / confirmar / liberar)")
    class Stock {
        @Test
        @DisplayName("reservarStock reserva la cantidad pedida en cada artículo del detalle")
        void reservarStock_reservaEnCadaArticulo() {
            Pedido pedido = pedidoConDetalles(TipoEnvio.TAKE_AWAY);
            ArticuloInsumo pizza = insumo("Pizza", 10000, 5, 20);
            detalle(pedido, pizza, 3);

            pedido.reservarStock();

            assertThat(pizza.getStockReservado()).isEqualTo(3.0);
        }

        @Test
        @DisplayName("reservarStock sin stock disponible propaga StockInsuficienteException")
        void reservarStock_sinStockDisponible_lanzaExcepcion() {
            Pedido pedido = pedidoConDetalles(TipoEnvio.TAKE_AWAY);
            detalle(pedido, insumo("Pizza", 10000, 2, 20), 5);

            assertThrows(StockInsuficienteException.class, pedido::reservarStock);
        }

        @Test
        @DisplayName("confirmarStock descuenta stock real y libera la reserva")
        void confirmarStock_descuentaYLiberaReserva() {
            Pedido pedido = pedidoConDetalles(TipoEnvio.TAKE_AWAY);
            ArticuloInsumo pizza = insumo("Pizza", 10000, 5, 20);
            detalle(pedido, pizza, 2);
            pedido.reservarStock();

            pedido.confirmarStock();

            assertThat(pizza.getStockActual()).isEqualTo(3.0);
            assertThat(pizza.getStockReservado()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("liberarStock resta de la reserva sin tocar el stock real")
        void liberarStock_restaDeReserva() {
            Pedido pedido = pedidoConDetalles(TipoEnvio.TAKE_AWAY);
            ArticuloInsumo pizza = insumo("Pizza", 10000, 5, 20);
            detalle(pedido, pizza, 2);
            pedido.reservarStock();

            pedido.liberarStock();

            assertThat(pizza.getStockReservado()).isEqualTo(0.0);
            assertThat(pizza.getStockActual()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("Sin detalles, ninguno de los tres explota")
        void sinDetalles_noExplota() {
            Pedido pedido = Pedido.builder().detallePedidos(List.of()).build();

            pedido.reservarStock();
            pedido.confirmarStock();
            pedido.liberarStock();
        }
    }

    @Nested
    @DisplayName("calcularCostoTotal")
    class CalcularCostoTotal {
        @Test
        @DisplayName("Suma el precio de costo de cada artículo por su cantidad")
        void sumaCostoPorCantidad() {
            Pedido pedido = pedidoConDetalles(TipoEnvio.TAKE_AWAY);
            detalle(pedido, insumo("Pizza", 10000, 5, 20), 2); // precioCosto = 5000

            pedido.calcularCostoTotal();

            assertThat(pedido.getCostoTotal()).isEqualByComparingTo("10000");
        }

        @Test
        @DisplayName("Un artículo sin precio de costo cargado hace fallar el cálculo")
        void sinPrecioCosto_lanzaExcepcion() {
            Pedido pedido = pedidoConDetalles(TipoEnvio.TAKE_AWAY);
            ArticuloInsumo sinCosto = insumo("Pizza", 10000, 5, 20);
            sinCosto.setPrecioCosto(null);
            detalle(pedido, sinCosto, 1);

            IllegalStateException ex = assertThrows(IllegalStateException.class, pedido::calcularCostoTotal);
            assertThat(ex.getMessage()).contains("Pizza");
        }

        @Test
        @DisplayName("Sin detalles, el costo total queda en cero")
        void sinDetalles_costoCero() {
            Pedido pedido = Pedido.builder().detallePedidos(List.of()).build();

            pedido.calcularCostoTotal();

            assertThat(pedido.getCostoTotal()).isEqualByComparingTo("0");
        }
    }
}
