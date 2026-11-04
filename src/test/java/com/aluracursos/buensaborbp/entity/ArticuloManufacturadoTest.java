package com.aluracursos.buensaborbp.entity;

import com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum;
import com.aluracursos.buensaborbp.exception.StockInsuficienteException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;

// ArticuloManufacturado tiene dos comportamientos distintos según esSemielaborado (ver el
// comentario en la entidad): un plato final SIEMPRE cascada a sus componentes (que pueden ser
// insumos o, a su vez, semielaborados), mientras que un semielaborado ajusta su propio stock
// igual que un ArticuloInsumo. Estos tests cubren ambos caminos y la mezcla de los dos.
class ArticuloManufacturadoTest {

    private static ArticuloInsumo insumo(String nombre, double precioCosto, UnidadMedidaEnum unidad,
                                          double stockActual) {
        return ArticuloInsumo.builder()
                .nombre(nombre)
                .precioVenta(BigDecimal.TEN)
                .precioCosto(BigDecimal.valueOf(precioCosto))
                .unidadMedidaEnum(unidad)
                .stockActual(stockActual).stockMaximo(1000.0).stockMinimo(1.0).stockReservado(0.0)
                .build();
    }

    // El stock de un semielaborado sale de sus lotes: se arma con un único lote vigente (vence en
    // un año) por esa cantidad.
    private static ArticuloManufacturado semielaborado(String nombre, double stockActual) {
        ArticuloManufacturado semi = semielaboradoSinLotes(nombre);
        agregarLote(semi, stockActual, LocalDate.now().plusYears(1));
        return semi;
    }

    private static ExistenciaSemielaborado agregarLote(ArticuloManufacturado semi, double cantidad, LocalDate vence) {
        ExistenciaSemielaborado lote = ExistenciaSemielaborado.builder()
                .semielaborado(semi).fechaElaboracion(LocalDate.now().minusDays(1)).fechaVencimiento(vence)
                .cantidad(cantidad).cantidadRestante(cantidad).build();
        semi.getExistencias().add(lote);
        semi.actualizarStockDesdeExistencias(LocalDate.now());
        return lote;
    }

    private static ArticuloManufacturado semielaboradoSinLotes(String nombre) {
        return ArticuloManufacturado.builder()
                .nombre(nombre)
                .precioVenta(BigDecimal.TEN)
                .precioCosto(BigDecimal.ONE)
                .unidadMedidaEnum(UnidadMedidaEnum.KG)
                .descripcion("desc").preparacion("prep")
                .esSemielaborado(true)
                .stockActual(0.0).stockMaximo(100.0).stockMinimo(1.0).stockReservado(0.0)
                .articuloManufacturadoDetalles(new ArrayList<>())
                .existencias(new ArrayList<>())
                .build();
    }

    private static ArticuloManufacturado platoFinalCon(ArticuloManufacturadoDetalle.ArticuloManufacturadoDetalleBuilder... detallesBuilders) {
        ArticuloManufacturado plato = ArticuloManufacturado.builder()
                .nombre("Plato")
                .precioVenta(BigDecimal.valueOf(5000))
                .unidadMedidaEnum(UnidadMedidaEnum.UNIDAD)
                .descripcion("desc").preparacion("prep")
                .esSemielaborado(false)
                .articuloManufacturadoDetalles(new ArrayList<>())
                .build();
        List<ArticuloManufacturadoDetalle> detalles = new ArrayList<>();
        for (ArticuloManufacturadoDetalle.ArticuloManufacturadoDetalleBuilder builder : detallesBuilders) {
            detalles.add(builder.articuloManufacturado(plato).build());
        }
        plato.setArticuloManufacturadoDetalles(detalles);
        return plato;
    }

    private static ArticuloManufacturadoDetalle.ArticuloManufacturadoDetalleBuilder detalle(Articulo componente, double cantidad) {
        return ArticuloManufacturadoDetalle.builder().componente(componente).cantidad(cantidad);
    }

    @Nested
    @DisplayName("calcularPrecioCosto")
    class CalcularPrecioCosto {
        @Test
        @DisplayName("Suma el costo de cada componente convertido a su unidad base")
        void sumaCostosConvertidos() {
            ArticuloInsumo queso = insumo("Queso", 4000, UnidadMedidaEnum.KG, 10.0);
            ArticuloInsumo sal = insumo("Sal", 1000, UnidadMedidaEnum.GR, 500.0);
            ArticuloManufacturado plato = platoFinalCon(detalle(queso, 0.2), detalle(sal, 5));

            plato.calcularPrecioCosto();

            // 0.2 kg * 4000 + (5 gr = 0.005 kg) * 1000 = 800 + 5
            assertThat(plato.getPrecioCosto()).isEqualByComparingTo("805");
        }

        @Test
        @DisplayName("Sin detalles, el costo queda en cero en vez de fallar")
        void sinDetalles_costoCero() {
            ArticuloManufacturado plato = platoFinalCon();

            plato.calcularPrecioCosto();

            assertThat(plato.getPrecioCosto()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("Un componente sin precio de costo definido rechaza el cálculo")
        void componenteSinCosto_lanzaExcepcion() {
            ArticuloInsumo sinCosto = ArticuloInsumo.builder()
                    .nombre("Sin costo").precioVenta(BigDecimal.TEN)
                    .unidadMedidaEnum(UnidadMedidaEnum.UNIDAD)
                    .stockActual(10.0).stockMaximo(10.0).stockMinimo(1.0).stockReservado(0.0)
                    .build();
            ArticuloManufacturado plato = platoFinalCon(detalle(sinCosto, 1));

            assertThrows(IllegalStateException.class, plato::calcularPrecioCosto);
        }
    }

    @Nested
    @DisplayName("calcularPrecioVenta")
    class CalcularPrecioVenta {
        @Test
        @DisplayName("Usa el precio manual cuando es mayor a cero")
        void conPrecioValido_loUsa() {
            ArticuloManufacturado plato = platoFinalCon();

            plato.calcularPrecioVenta(BigDecimal.valueOf(7500));

            assertThat(plato.getPrecioVenta()).isEqualByComparingTo("7500");
        }

        @Test
        @DisplayName("Rechaza precio nulo o menor/igual a cero (un plato final no tiene margen automático)")
        void conPrecioInvalido_lanzaExcepcion() {
            ArticuloManufacturado plato = platoFinalCon();

            assertThrows(IllegalArgumentException.class, () -> plato.calcularPrecioVenta(null));
            assertThrows(IllegalArgumentException.class, () -> plato.calcularPrecioVenta(BigDecimal.ZERO));
        }
    }

    @Nested
    @DisplayName("reservarStock")
    class ReservarStock {
        @Test
        @DisplayName("Plato final: cascada la reserva a cada componente multiplicando por la cantidad pedida")
        void platoFinal_cascadaAComponentes() {
            ArticuloInsumo queso = insumo("Queso", 4000, UnidadMedidaEnum.KG, 10.0);
            ArticuloManufacturado plato = platoFinalCon(detalle(queso, 0.2));

            plato.reservarStock(3, "Pedido #1"); // 3 platos -> 0.6 kg de queso

            assertThat(queso.getStockReservado()).isCloseTo(0.6, within(0.0001));
        }

        @Test
        @DisplayName("Plato final con un semielaborado como componente: la cascada llega hasta el semielaborado, no sigue bajando")
        void platoFinal_cascadaHastaSemielaborado() {
            ArticuloManufacturado masa = semielaborado("Masa de pizza", 10.0);
            ArticuloManufacturado plato = platoFinalCon(detalle(masa, 0.3));

            plato.reservarStock(2, "Pedido #2"); // 2 platos -> 0.6 kg de masa

            assertThat(masa.getStockReservado()).isEqualTo(0.6);
        }

        @Test
        @DisplayName("Plato final: si un componente no tiene stock suficiente, la excepción indica el nombre del padre")
        void platoFinal_componenteSinStock_lanzaExcepcion() {
            ArticuloInsumo queso = insumo("Queso", 4000, UnidadMedidaEnum.KG, 1.0);
            ArticuloManufacturado plato = platoFinalCon(detalle(queso, 1));
            plato.setNombre("Pizza Muzzarella");

            StockInsuficienteException ex = assertThrows(StockInsuficienteException.class,
                    () -> plato.reservarStock(5, plato.getNombre()));

            assertThat(ex.getMessage()).contains("Queso").contains("Pizza Muzzarella");
        }

        @Test
        @DisplayName("Semielaborado: reserva de su propio stock convertido a unidad base, sin cascada")
        void semielaborado_reservaStockPropio() {
            ArticuloManufacturado masa = semielaborado("Masa de pizza", 10.0);

            masa.reservarStock(4, "Pedido #3");

            assertThat(masa.getStockReservado()).isEqualTo(4.0);
        }

        @Test
        @DisplayName("Semielaborado sin stock suficiente lanza StockInsuficienteException")
        void semielaborado_sinStockSuficiente_lanzaExcepcion() {
            ArticuloManufacturado masa = semielaborado("Masa de pizza", 2.0);

            assertThrows(StockInsuficienteException.class, () -> masa.reservarStock(5, "Pedido #4"));
        }
    }

    @Nested
    @DisplayName("liberarStock")
    class LiberarStock {
        @Test
        @DisplayName("Plato final: libera cascadeando a cada componente")
        void platoFinal_cascadaAComponentes() {
            ArticuloInsumo queso = insumo("Queso", 4000, UnidadMedidaEnum.KG, 10.0);
            queso.setStockReservado(1.0);
            ArticuloManufacturado plato = platoFinalCon(detalle(queso, 0.2));

            plato.liberarStock(3); // libera 0.6 kg

            assertThat(queso.getStockReservado()).isCloseTo(0.4, within(0.0001));
        }

        @Test
        @DisplayName("Semielaborado: libera de su propia reserva sin bajar de cero")
        void semielaborado_noBajaDeCero() {
            ArticuloManufacturado masa = semielaborado("Masa de pizza", 10.0);
            masa.setStockReservado(1.0);

            masa.liberarStock(5);

            assertThat(masa.getStockReservado()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("confirmarStock")
    class ConfirmarStock {
        @Test
        @DisplayName("Plato final: confirma cascadeando a cada componente")
        void platoFinal_cascadaAComponentes() {
            ArticuloInsumo queso = insumo("Queso", 4000, UnidadMedidaEnum.KG, 10.0);
            queso.setStockReservado(1.0);
            ArticuloManufacturado plato = platoFinalCon(detalle(queso, 0.2));

            plato.confirmarStock(3); // confirma 0.6 kg

            assertThat(queso.getStockActual()).isCloseTo(9.4, within(0.0001));
            assertThat(queso.getStockReservado()).isCloseTo(0.4, within(0.0001));
        }

        @Test
        @DisplayName("Semielaborado: descuenta de su propio stock real y de su reserva")
        void semielaborado_descuentaStockPropio() {
            ArticuloManufacturado masa = semielaborado("Masa de pizza", 10.0);
            masa.setStockReservado(4.0);

            masa.confirmarStock(4);

            assertThat(masa.getStockActual()).isEqualTo(6.0);
            assertThat(masa.getStockReservado()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Semielaborado sin reserva suficiente lanza IllegalStateException y no toca el stock")
        void semielaborado_sinReservaSuficiente_lanzaExcepcion() {
            ArticuloManufacturado masa = semielaborado("Masa de pizza", 10.0);
            masa.setStockReservado(1.0);

            assertThrows(IllegalStateException.class, () -> masa.confirmarStock(4));

            assertThat(masa.getStockActual()).isEqualTo(10.0);
        }
    }

    // Lotes (existencias): el stock es la suma de los vigentes y los pedidos consumen primero el
    // que vence antes.
    @Nested
    @DisplayName("lotes de semielaborado")
    class Lotes {
        @Test
        @DisplayName("Confirmar consume primero el lote que vence antes, y sigue con el siguiente")
        void consumeFifoPorVencimiento() {
            ArticuloManufacturado masa = semielaboradoSinLotes("Masa de pizza");
            ExistenciaSemielaborado tardio = agregarLote(masa, 5.0, LocalDate.now().plusDays(10));
            ExistenciaSemielaborado temprano = agregarLote(masa, 3.0, LocalDate.now().plusDays(2));
            masa.reservarStock(4, "Pedido #1");

            masa.confirmarStock(4);

            assertThat(temprano.getCantidadRestante()).isEqualTo(0.0);
            assertThat(tardio.getCantidadRestante()).isEqualTo(4.0);
            assertThat(masa.getStockActual()).isEqualTo(4.0);
        }

        @Test
        @DisplayName("Un lote vencido no cuenta como stock ni se puede reservar")
        void loteVencido_noCuenta() {
            ArticuloManufacturado masa = semielaboradoSinLotes("Masa de pizza");
            agregarLote(masa, 8.0, LocalDate.now().minusDays(1));
            agregarLote(masa, 2.0, LocalDate.now().plusDays(5));

            assertThat(masa.getStockActual()).isEqualTo(2.0);
            assertThrows(StockInsuficienteException.class, () -> masa.reservarStock(3, "Pedido #2"));
        }

        @Test
        @DisplayName("Un lote descartado no cuenta")
        void loteDescartado_noCuenta() {
            ArticuloManufacturado masa = semielaboradoSinLotes("Masa de pizza");
            ExistenciaSemielaborado lote = agregarLote(masa, 6.0, LocalDate.now().plusDays(5));
            lote.setDescartado(true);

            masa.actualizarStockDesdeExistencias(LocalDate.now());

            assertThat(masa.getStockActual()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("stock mínimo")
    class StockMinimo {
        @Test
        @DisplayName("Solo aplica al semielaborado, que es el único con stock propio")
        void tieneStockMinimo_soloTieneSentidoEnSemielaborado() {
            ArticuloManufacturado masa = semielaborado("Masa de pizza", 1.0);
            masa.setStockMinimo(2.0);

            assertThat(masa.tieneStockMinimo()).isTrue();
        }

        @Test
        @DisplayName("Un plato final sin stock propio nunca está en stock mínimo")
        void platoFinal_sinStockPropio_nuncaEnMinimo() {
            ArticuloManufacturado plato = platoFinalCon();

            assertThat(plato.tieneStockMinimo()).isFalse();
        }
    }
}
