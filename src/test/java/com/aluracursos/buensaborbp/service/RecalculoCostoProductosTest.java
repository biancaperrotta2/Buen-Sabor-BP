package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.entity.ArticuloInsumo;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturado;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturadoDetalle;
import com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum;
import com.aluracursos.buensaborbp.repository.ArticuloManufacturadoDetalleRepository;
import com.aluracursos.buensaborbp.repository.ArticuloManufacturadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

// El costo guardado de un producto tiene que seguir al de sus ingredientes; si no, queda con el
// valor del día en que se guardó (ej. Hamburguesa Cheese quedó en $2.300.500 en vez de ~$2.800).
@ExtendWith(MockitoExtension.class)
class RecalculoCostoProductosTest {

    @Mock private ArticuloManufacturadoDetalleRepository detalleRepository;
    @Mock private ArticuloManufacturadoRepository manufacturadoRepository;

    private RecalculoCostoProductos recalculo;

    @BeforeEach
    void setUp() {
        recalculo = new RecalculoCostoProductos(detalleRepository, manufacturadoRepository);
    }

    private ArticuloInsumo carne(double precioPorKg) {
        return ArticuloInsumo.builder().id(17L).nombre("Carne molida")
                .precioVenta(BigDecimal.TEN).precioCosto(BigDecimal.valueOf(precioPorKg))
                .unidadMedidaEnum(UnidadMedidaEnum.GR)
                .stockActual(7.0).stockMinimo(1.0).stockMaximo(10.0).build();
    }

    private ArticuloManufacturado producto(long id, String nombre, boolean semielaborado) {
        return ArticuloManufacturado.builder().id(id).nombre(nombre)
                .precioVenta(BigDecimal.valueOf(10000)).precioCosto(BigDecimal.valueOf(2_300_500))
                .unidadMedidaEnum(semielaborado ? UnidadMedidaEnum.KG : UnidadMedidaEnum.UNIDAD)
                .descripcion("d").preparacion("p").esSemielaborado(semielaborado)
                .articuloManufacturadoDetalles(new ArrayList<>()).build();
    }

    private ArticuloManufacturadoDetalle usar(ArticuloManufacturado producto, Object componente, double cantidad) {
        ArticuloManufacturadoDetalle d = ArticuloManufacturadoDetalle.builder()
                .articuloManufacturado(producto).componente((com.aluracursos.buensaborbp.entity.Articulo) componente)
                .cantidad(cantidad).build();
        producto.getArticuloManufacturadoDetalles().add(d);
        return d;
    }

    @Test
    @DisplayName("Recalcula el costo guardado de cada producto que usa el insumo")
    void recalculaProductosQueUsanElInsumo() {
        ArticuloInsumo carne = carne(8000);
        ArticuloManufacturado hamburguesa = producto(21L, "Hamburguesa Cheese", false);
        when(detalleRepository.findByComponente_Id(17L)).thenReturn(List.of(usar(hamburguesa, carne, 250)));

        recalculo.recalcularProductosQueUsan(17L);

        assertThat(hamburguesa.getPrecioCosto()).isEqualByComparingTo("2000"); // 0,25 kg x 8000
        verify(manufacturadoRepository).save(hamburguesa);
    }

    @Test
    @DisplayName("Un producto que usa el insumo dos veces se recalcula y guarda una sola vez")
    void productoRepetido_unaSolaVez() {
        ArticuloInsumo carne = carne(8000);
        ArticuloManufacturado hamburguesa = producto(21L, "Doble", false);
        when(detalleRepository.findByComponente_Id(17L))
                .thenReturn(List.of(usar(hamburguesa, carne, 250), usar(hamburguesa, carne, 250)));

        recalculo.recalcularProductosQueUsan(17L);

        verify(manufacturadoRepository, times(1)).save(hamburguesa);
        assertThat(hamburguesa.getPrecioCosto()).isEqualByComparingTo("4000");
    }

    @Test
    @DisplayName("Si el producto recalculado es un semielaborado, sigue hacia los productos que lo usan")
    void semielaborado_propagaAlPlatoQueLoUsa() {
        ArticuloInsumo carne = carne(8000);
        ArticuloManufacturado relleno = producto(30L, "Relleno de carne", true);
        ArticuloManufacturado empanada = producto(31L, "Empanada", false);
        when(detalleRepository.findByComponente_Id(17L)).thenReturn(List.of(usar(relleno, carne, 1000)));
        when(detalleRepository.findByComponente_Id(30L)).thenReturn(List.of(usar(empanada, relleno, 0.5)));

        recalculo.recalcularProductosQueUsan(17L);

        assertThat(relleno.getPrecioCosto()).isEqualByComparingTo("8000");  // 1 kg x 8000
        assertThat(empanada.getPrecioCosto()).isEqualByComparingTo("4000"); // 0,5 kg x 8000
        verify(manufacturadoRepository).save(empanada);
    }

    @Test
    @DisplayName("Sin productos que lo usen, no guarda nada")
    void sinUsos_noGuardaNada() {
        when(detalleRepository.findByComponente_Id(17L)).thenReturn(List.of());

        recalculo.recalcularProductosQueUsan(17L);

        verifyNoInteractions(manufacturadoRepository);
    }
}
