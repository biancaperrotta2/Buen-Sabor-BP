package com.aluracursos.buensaborbp.entity;

import com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Promocion es la funcionalidad agregada el 2026-09-23 (ver "Promociones: full admin CRUD" en
// CLAUDE.md) — nadie había podido ejercitar este cálculo antes porque no existía ningún
// controlador que llamara a crearPromocion. Acá se prueba la lógica de precio en aislamiento.
class PromocionTest {

    private static ArticuloInsumo insumo(String nombre, double precioVenta, double precioCosto) {
        return ArticuloInsumo.builder()
                .nombre(nombre)
                .precioVenta(BigDecimal.valueOf(precioVenta))
                .precioCosto(BigDecimal.valueOf(precioCosto))
                .unidadMedidaEnum(UnidadMedidaEnum.UNIDAD)
                .stockActual(10.0).stockMaximo(50.0).stockMinimo(1.0).stockReservado(0.0)
                .build();
    }

    private static ArticuloManufacturado productoFinal(String nombre, double precioVenta, double precioCosto) {
        return ArticuloManufacturado.builder()
                .nombre(nombre)
                .precioVenta(BigDecimal.valueOf(precioVenta))
                .precioCosto(BigDecimal.valueOf(precioCosto))
                .unidadMedidaEnum(UnidadMedidaEnum.UNIDAD)
                .descripcion("desc").preparacion("prep")
                .esSemielaborado(false)
                .build();
    }

    private static Promocion promocionCon(Articulo... items) {
        Promocion promo = Promocion.builder()
                .nombre("Combo")
                .promocionDetalle(new ArrayList<>())
                .build();
        for (Articulo item : items) {
            promo.getPromocionDetalle().add(
                    PromocionDetalle.builder().articulos(item).cantidad(1).articuloPromocion(promo).build());
        }
        return promo;
    }

    @Nested
    @DisplayName("calcularPrecioCosto")
    class CalcularPrecioCosto {
        @Test
        @DisplayName("Con un insumo, convierte la cantidad a unidad base antes de multiplicar por el costo")
        void conInsumo_convierteAUnidadBase() {
            ArticuloInsumo bebida = insumo("Bebida", 2000, 800);
            Promocion promo = promocionCon(bebida);

            promo.calcularPrecioCosto();

            assertThat(promo.getPrecioCosto()).isEqualByComparingTo("800");
        }

        @Test
        @DisplayName("Con un producto final, multiplica el costo directo por la cantidad (sin conversión)")
        void conProductoFinal_multiplicaDirecto() {
            ArticuloManufacturado hamburguesa = productoFinal("Hamburguesa", 8000, 3000);
            Promocion promo = promocionCon(hamburguesa);

            promo.calcularPrecioCosto();

            assertThat(promo.getPrecioCosto()).isEqualByComparingTo("3000");
        }

        @Test
        @DisplayName("Suma el costo de todos los ítems del combo")
        void variosItems_sumaCostos() {
            Promocion promo = promocionCon(
                    productoFinal("Hamburguesa", 8000, 3000),
                    insumo("Bebida", 2000, 800));

            promo.calcularPrecioCosto();

            assertThat(promo.getPrecioCosto()).isEqualByComparingTo("3800");
        }

        @Test
        @DisplayName("Sin detalles, rechaza calcular (una promo necesita al menos un ítem)")
        void sinDetalles_lanzaExcepcion() {
            Promocion promo = Promocion.builder().nombre("Vacía").promocionDetalle(new ArrayList<>()).build();

            assertThrows(IllegalStateException.class, promo::calcularPrecioCosto);
        }
    }

    @Nested
    @DisplayName("calcularPrecioTotal")
    class CalcularPrecioTotal {
        @Test
        @DisplayName("Suma el precio de venta de lista de cada ítem (el 'precio sin descuento')")
        void sumaPreciosDeVenta() {
            Promocion promo = promocionCon(
                    productoFinal("Hamburguesa", 8000, 3000),
                    insumo("Bebida", 2000, 800));

            promo.calcularPrecioTotal();

            assertThat(promo.getPrecioTotal()).isEqualByComparingTo("10000");
        }
    }

    @Nested
    @DisplayName("calcularPrecioVenta")
    class CalcularPrecioVentaTest {
        @Test
        @DisplayName("Con precio manual, lo usa tal cual (así se carga el descuento real del combo)")
        void conPrecioManual_loUsa() {
            Promocion promo = promocionCon(productoFinal("Hamburguesa", 8000, 3000));

            promo.calcularPrecioVenta(BigDecimal.valueOf(6500));

            assertThat(promo.getPrecioVenta()).isEqualByComparingTo("6500");
        }

        @Test
        @DisplayName("Sin precio manual, usa el precio de lista (calculándolo si hace falta)")
        void sinPrecioManual_usaPrecioTotal() {
            Promocion promo = promocionCon(productoFinal("Hamburguesa", 8000, 3000));

            promo.calcularPrecioVenta(null);

            assertThat(promo.getPrecioVenta()).isEqualByComparingTo("8000");
        }
    }
}
