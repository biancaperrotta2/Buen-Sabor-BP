package com.aluracursos.buensaborbp.entity;

import com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum;
import com.aluracursos.buensaborbp.exception.StockInsuficienteException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// calcularPrecioVenta() fue el centro del bloque 2 de bugs (2026-09-21): el formulario viejo
// mandaba siempre margen=0, lo que pisaba en silencio cualquier precio manual cargado. Ver
// CLAUDE.md, "Margen AND precio de venta both required".
class ArticuloInsumoTest {

    private static ArticuloInsumo.ArticuloInsumoBuilder<?, ?> base() {
        return ArticuloInsumo.builder()
                .nombre("Coca 500ml")
                .unidadMedidaEnum(UnidadMedidaEnum.UNIDAD)
                .stockActual(10.0)
                .stockMaximo(50.0)
                .stockMinimo(5.0)
                .stockReservado(0.0);
    }

    @Nested
    @DisplayName("calcularPrecioVenta")
    class CalcularPrecioVenta {

        @Test
        @DisplayName("Con margen positivo, calcula precio = costo * (1 + margen/100)")
        void conMargenPositivo_calculaPrecio() {
            ArticuloInsumo insumo = base().precioCosto(BigDecimal.valueOf(100)).margen(50f).build();

            insumo.calcularPrecioVenta(null);

            assertThat(insumo.getPrecioVenta()).isEqualByComparingTo("150");
        }

        @Test
        @DisplayName("Con margen explícitamente en 0, el precio de venta queda igual al costo")
        void conMargenCero_usaPrecioCosto() {
            ArticuloInsumo insumo = base().precioCosto(BigDecimal.valueOf(100)).margen(0f).build();

            insumo.calcularPrecioVenta(null);

            assertThat(insumo.getPrecioVenta()).isEqualByComparingTo("100");
        }

        @Test
        @DisplayName("Sin margen (null), usa el precio manual tal cual se lo pasaron")
        void sinMargen_usaPrecioManual() {
            ArticuloInsumo insumo = base().precioCosto(BigDecimal.valueOf(100)).margen(null).build();

            insumo.calcularPrecioVenta(BigDecimal.valueOf(180));

            assertThat(insumo.getPrecioVenta()).isEqualByComparingTo("180");
        }

        @Test
        @DisplayName("Con margen pero sin precio de costo todavía, usa el precio manual")
        void conMargenSinCosto_usaPrecioManual() {
            ArticuloInsumo insumo = base().precioCosto(null).margen(50f).build();

            insumo.calcularPrecioVenta(BigDecimal.valueOf(200));

            assertThat(insumo.getPrecioVenta()).isEqualByComparingTo("200");
        }
    }

    @Nested
    @DisplayName("reservarStock")
    class ReservarStock {
        @Test
        @DisplayName("Con stock disponible, suma a stockReservado (convertido a unidad base)")
        void conStockDisponible_reserva() {
            ArticuloInsumo insumo = base().precioVenta(BigDecimal.TEN).stockActual(10.0).build();

            insumo.reservarStock(3, "Pizza Muzzarella");

            assertThat(insumo.getStockReservado()).isEqualTo(3.0);
            assertThat(insumo.getStockDisponible()).isEqualTo(7.0);
        }

        @Test
        @DisplayName("Sin stock disponible suficiente, lanza StockInsuficienteException con el nombre del padre")
        void sinStockSuficiente_lanzaExcepcion() {
            ArticuloInsumo insumo = base().precioVenta(BigDecimal.TEN).stockActual(2.0).build();

            StockInsuficienteException ex = assertThrows(StockInsuficienteException.class,
                    () -> insumo.reservarStock(5, "Pizza Muzzarella"));

            assertThat(ex.getMessage()).contains("Coca 500ml").contains("Pizza Muzzarella");
        }

        @Test
        @DisplayName("Convierte la cantidad a la unidad base antes de reservar (ej. GR)")
        void convierteAUnidadBase() {
            ArticuloInsumo insumo = base().precioVenta(BigDecimal.TEN)
                    .unidadMedidaEnum(UnidadMedidaEnum.GR)
                    .stockActual(1.0) // 1 kg disponible, GR.getFactor() = 0.001
                    .build();

            insumo.reservarStock(500, "Receta"); // 500 gr = 0.5 kg

            assertThat(insumo.getStockReservado()).isEqualTo(0.5);
        }
    }

    @Nested
    @DisplayName("confirmarStock")
    class ConfirmarStock {
        @Test
        @DisplayName("Con reserva suficiente, descuenta del stock real y de la reserva")
        void conReservaSuficiente_descuenta() {
            ArticuloInsumo insumo = base().precioVenta(BigDecimal.TEN).stockActual(10.0).stockReservado(3.0).build();

            insumo.confirmarStock(3);

            assertThat(insumo.getStockActual()).isEqualTo(7.0);
            assertThat(insumo.getStockReservado()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Sin reserva suficiente, lanza IllegalStateException y no toca el stock")
        void sinReservaSuficiente_lanzaExcepcion() {
            ArticuloInsumo insumo = base().precioVenta(BigDecimal.TEN).stockActual(10.0).stockReservado(1.0).build();

            assertThrows(IllegalStateException.class, () -> insumo.confirmarStock(3));

            assertThat(insumo.getStockActual()).isEqualTo(10.0);
        }
    }

    @Nested
    @DisplayName("liberarStock")
    class LiberarStock {
        @Test
        @DisplayName("Resta de stockReservado sin tocar el stock real")
        void restaDeReserva() {
            ArticuloInsumo insumo = base().precioVenta(BigDecimal.TEN).stockActual(10.0).stockReservado(4.0).build();

            insumo.liberarStock(3);

            assertThat(insumo.getStockReservado()).isEqualTo(1.0);
            assertThat(insumo.getStockActual()).isEqualTo(10.0);
        }

        @Test
        @DisplayName("Nunca deja la reserva negativa, aunque se libere de más")
        void noBajaDeCero() {
            ArticuloInsumo insumo = base().precioVenta(BigDecimal.TEN).stockActual(10.0).stockReservado(1.0).build();

            insumo.liberarStock(5);

            assertThat(insumo.getStockReservado()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("stock mínimo")
    class StockMinimo {
        @Test
        @DisplayName("tieneStockMinimo es true cuando el stock actual es igual o menor al mínimo")
        void igualOMenorAlMinimo_esTrue() {
            ArticuloInsumo enElMinimo = base().precioVenta(BigDecimal.TEN).stockActual(5.0).stockMinimo(5.0).build();
            ArticuloInsumo porDebajo = base().precioVenta(BigDecimal.TEN).stockActual(3.0).stockMinimo(5.0).build();
            ArticuloInsumo porEncima = base().precioVenta(BigDecimal.TEN).stockActual(6.0).stockMinimo(5.0).build();

            assertThat(enElMinimo.tieneStockMinimo()).isTrue();
            assertThat(porDebajo.tieneStockMinimo()).isTrue();
            assertThat(porEncima.tieneStockMinimo()).isFalse();
        }

        @Test
        @DisplayName("obtenerInsumosConStockMinimo filtra solo los que están en o por debajo del mínimo")
        void obtenerInsumosConStockMinimo_filtraCorrectamente() {
            ArticuloInsumo bajo = base().precioVenta(BigDecimal.TEN).stockActual(1.0).stockMinimo(5.0).build();
            ArticuloInsumo ok = base().precioVenta(BigDecimal.TEN).stockActual(20.0).stockMinimo(5.0).build();

            List<ArticuloInsumo> resultado = ArticuloInsumo.obtenerInsumosConStockMinimo(List.of(bajo, ok));

            assertThat(resultado).containsExactly(bajo);
        }

        @Test
        @DisplayName("Con lista null, devuelve lista vacía en vez de explotar")
        void listaNull_devuelveVacia() {
            assertThat(ArticuloInsumo.obtenerInsumosConStockMinimo(null)).isEmpty();
        }
    }

    // Inversa de convertirACantidadBase (ya probado indirectamente vía reservarStock arriba):
    // de la unidad base al valor en la unidad elegida. Usada por ArticuloInsumoServiceImpl
    // para mostrar/recibir el stock en la unidad que el admin tiene seleccionada (ver
    // ArticuloInsumoServiceImplTest, "actualiza stock").
    @Nested
    @DisplayName("convertirDesdeCantidadBase")
    class ConvertirDesdeCantidadBase {
        @Test
        @DisplayName("De la unidad base a Gramos, multiplica por 1000")
        void deBaseAGramos() {
            ArticuloInsumo insumo = base().precioVenta(BigDecimal.TEN).unidadMedidaEnum(UnidadMedidaEnum.GR).build();

            assertThat(insumo.convertirDesdeCantidadBase(1.85)).isEqualTo(1850.0);
        }

        @Test
        @DisplayName("En una unidad ya base (Kilos), no cambia el valor")
        void enUnidadBase_noCambia() {
            ArticuloInsumo insumo = base().precioVenta(BigDecimal.TEN).unidadMedidaEnum(UnidadMedidaEnum.KG).build();

            assertThat(insumo.convertirDesdeCantidadBase(2.0)).isEqualTo(2.0);
        }

        @Test
        @DisplayName("Es la inversa exacta de convertirACantidadBase")
        void esInversaDeConvertirACantidadBase() {
            ArticuloInsumo insumo = base().precioVenta(BigDecimal.TEN).unidadMedidaEnum(UnidadMedidaEnum.GR).build();

            double base = insumo.convertirACantidadBase(500);

            assertThat(insumo.convertirDesdeCantidadBase(base)).isEqualTo(500.0);
        }
    }
}
