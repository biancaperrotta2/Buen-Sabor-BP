package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.ExistenciaInsumoRequest;
import com.aluracursos.buensaborbp.dto.ExistenciaInsumoResponse;
import com.aluracursos.buensaborbp.entity.ArticuloInsumo;
import com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum;
import com.aluracursos.buensaborbp.entity.ExistenciaInsumo;
import com.aluracursos.buensaborbp.repository.ArticuloInsumoRepository;
import com.aluracursos.buensaborbp.repository.ExistenciaInsumoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Compras de insumo (registro simple): suman/ajustan/descuentan stock actual sin bajar de lo
// reservado, y el precio de costo pasa a ser el de la compra más reciente.
@ExtendWith(MockitoExtension.class)
class ExistenciaInsumoServiceTest {

    @Mock private ArticuloInsumoRepository insumoRepository;
    @Mock private ExistenciaInsumoRepository existenciaRepository;
    @Mock private RecalculoCostoProductos recalculoCostoProductos;

    private ExistenciaInsumoService service;
    private ArticuloInsumo harina;
    private final LocalDate hoy = LocalDate.now();

    @BeforeEach
    void setUp() {
        service = new ExistenciaInsumoService(insumoRepository, existenciaRepository, recalculoCostoProductos);
        // Harina en Gramos: stock guardado en kg. 2 kg, $1000 el kg, sin margen (precio manual $5).
        harina = ArticuloInsumo.builder().id(5L).nombre("Harina 000").unidadMedidaEnum(UnidadMedidaEnum.GR)
                .precioVenta(BigDecimal.valueOf(5)).precioCosto(BigDecimal.valueOf(1000))
                .stockActual(2.0).stockMinimo(1.0).stockMaximo(20.0).stockReservado(0.0).build();
        lenient().when(insumoRepository.findById(5L)).thenReturn(Optional.of(harina));
        lenient().when(existenciaRepository.save(any(ExistenciaInsumo.class))).thenAnswer(inv -> {
            ExistenciaInsumo e = inv.getArgument(0);
            if (e.getId() == null) e.setId(100L);
            return e;
        });
    }

    private ExistenciaInsumo compraGuardada(double cantidadKg, long costo) {
        ExistenciaInsumo e = ExistenciaInsumo.builder().id(100L).insumo(harina)
                .fechaCompra(hoy.minusDays(2)).fechaVencimiento(hoy.plusMonths(3))
                .cantidad(cantidadKg).costoUnitario(BigDecimal.valueOf(costo)).build();
        when(existenciaRepository.findById(100L)).thenReturn(Optional.of(e));
        return e;
    }

    private void ultimaCompraCuesta(long costo) {
        when(existenciaRepository.findFirstByInsumo_IdOrderByFechaCompraDescIdDesc(5L))
                .thenReturn(Optional.of(ExistenciaInsumo.builder().costoUnitario(BigDecimal.valueOf(costo)).build()));
    }

    @Test
    @DisplayName("Cargar 3000 g suma 3 kg al stock y la compra se ve en gramos con su costo total")
    void crear_sumaStock() {
        ultimaCompraCuesta(1200);
        ExistenciaInsumoResponse r = service.crear(5L,
                new ExistenciaInsumoRequest(null, 3000.0, BigDecimal.valueOf(1200), hoy.plusMonths(6)));

        assertThat(harina.getStockActual()).isEqualTo(5.0);
        assertThat(r.cantidad()).isEqualTo(3000.0);
        assertThat(r.costoTotal()).isEqualByComparingTo("3600");
        assertThat(r.fechaCompra()).isEqualTo(hoy);
        assertThat(r.vencida()).isFalse();
    }

    @Test
    @DisplayName("El precio de costo pasa a ser el de la última compra y se recalculan los productos")
    void crear_actualizaCostoYRecalcula() {
        ultimaCompraCuesta(1200);
        service.crear(5L, new ExistenciaInsumoRequest(null, 1000.0, BigDecimal.valueOf(1200), hoy.plusDays(30)));

        assertThat(harina.getPrecioCosto()).isEqualByComparingTo("1200");
        assertThat(harina.getPrecioVenta()).isEqualByComparingTo("5"); // sin margen: queda el manual
        verify(recalculoCostoProductos).recalcularProductosQueUsan(5L);
    }

    @Test
    @DisplayName("Con margen, el precio de venta sigue al nuevo costo")
    void crear_conMargen_recalculaPrecioVenta() {
        harina.setMargen(50f);
        ultimaCompraCuesta(2000);
        service.crear(5L, new ExistenciaInsumoRequest(null, 1000.0, BigDecimal.valueOf(2000), hoy.plusDays(30)));

        assertThat(harina.getPrecioVenta()).isEqualByComparingTo("3000");
    }

    @Test
    @DisplayName("Si el costo no cambia, no recalcula productos")
    void crear_mismoCosto_noRecalcula() {
        ultimaCompraCuesta(1000);
        service.crear(5L, new ExistenciaInsumoRequest(null, 1000.0, BigDecimal.valueOf(1000), hoy.plusDays(30)));

        verifyNoInteractions(recalculoCostoProductos);
    }

    @Test
    @DisplayName("Fecha de compra futura o vencimiento anterior a la compra se rechazan sin tocar el stock")
    void crear_fechasInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> service.crear(5L,
                new ExistenciaInsumoRequest(hoy.plusDays(1), 1000.0, BigDecimal.TEN, hoy.plusDays(30))));
        assertThrows(IllegalArgumentException.class, () -> service.crear(5L,
                new ExistenciaInsumoRequest(hoy, 1000.0, BigDecimal.TEN, hoy.minusDays(1))));
        assertThat(harina.getStockActual()).isEqualTo(2.0);
        verify(existenciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Corregir una compra de 1 kg a 1500 g suma solo la diferencia")
    void editar_ajustaDiferencia() {
        compraGuardada(1.0, 1000);
        ultimaCompraCuesta(1000);
        service.editar(100L, new ExistenciaInsumoRequest(hoy.minusDays(2), 1500.0, BigDecimal.valueOf(1000), hoy.plusMonths(3)));

        assertThat(harina.getStockActual()).isEqualTo(2.5);
    }

    @Test
    @DisplayName("Borrar una compra descuenta su cantidad del stock")
    void eliminar_descuenta() {
        ExistenciaInsumo e = compraGuardada(1.5, 1000);
        service.eliminar(100L);

        assertThat(harina.getStockActual()).isEqualTo(0.5);
        verify(existenciaRepository).delete(e);
    }

    @Test
    @DisplayName("No se puede borrar si el stock quedaría por debajo de lo reservado por pedidos")
    void eliminar_respetaReservado() {
        harina.setStockReservado(1.0);
        compraGuardada(1.5, 1000);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.eliminar(100L));
        assertThat(ex.getMessage()).contains("reservados");
        assertThat(harina.getStockActual()).isEqualTo(2.0);
        verify(existenciaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("No se puede borrar una compra que ya se usó (el stock quedaría negativo)")
    void eliminar_compraYaUsada() {
        compraGuardada(3.0, 1000);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.eliminar(100L));
        assertThat(ex.getMessage()).contains("ya se usó");
    }
}
