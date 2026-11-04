package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.ExistenciaSemielaboradoRequest;
import com.aluracursos.buensaborbp.dto.ExistenciaSemielaboradoResponse;
import com.aluracursos.buensaborbp.entity.ArticuloInsumo;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturado;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturadoDetalle;
import com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum;
import com.aluracursos.buensaborbp.entity.ExistenciaSemielaborado;
import com.aluracursos.buensaborbp.repository.ArticuloManufacturadoRepository;
import com.aluracursos.buensaborbp.repository.ExistenciaSemielaboradoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Lotes de semielaborado: preparar descuenta insumos según la receta (escrita para 1 unidad del
// semielaborado), corregir o borrar un lote sin usar devuelve la diferencia, y un vencido se da de baja.
@ExtendWith(MockitoExtension.class)
class ExistenciaSemielaboradoServiceTest {

    @Mock private ArticuloManufacturadoRepository manufacturadoRepository;
    @Mock private ExistenciaSemielaboradoRepository existenciaRepository;

    private ExistenciaSemielaboradoService service;
    private ArticuloInsumo harina;
    private ArticuloManufacturado masa;

    @BeforeEach
    void setUp() {
        service = new ExistenciaSemielaboradoService(manufacturadoRepository, existenciaRepository);
        // Harina en Gramos: stock guardado en kg (base). 10 kg disponibles, cuesta $2000 el kg.
        harina = ArticuloInsumo.builder().id(5L).nombre("Harina 000").unidadMedidaEnum(UnidadMedidaEnum.GR)
                .precioVenta(BigDecimal.ONE).precioCosto(BigDecimal.valueOf(2000))
                .stockActual(10.0).stockMinimo(1.0).stockMaximo(20.0).stockReservado(0.0).build();
        // Masa en Kilos: 1 kg de masa lleva 600 g de harina.
        masa = ArticuloManufacturado.builder().id(30L).nombre("Masa de pizza").unidadMedidaEnum(UnidadMedidaEnum.KG)
                .precioVenta(BigDecimal.ZERO).descripcion("d").preparacion("").esSemielaborado(true)
                .stockActual(0.0).stockMinimo(1.0).stockMaximo(20.0).stockReservado(0.0)
                .articuloManufacturadoDetalles(new ArrayList<>()).existencias(new ArrayList<>()).build();
        masa.getArticuloManufacturadoDetalles().add(ArticuloManufacturadoDetalle.builder()
                .articuloManufacturado(masa).componente(harina).cantidad(600.0).build());
        lenient().when(manufacturadoRepository.findById(30L)).thenReturn(Optional.of(masa));
    }

    private ExistenciaSemielaborado lote(double cantidad, double restante, LocalDate vence) {
        ExistenciaSemielaborado e = ExistenciaSemielaborado.builder().id(100L).semielaborado(masa)
                .fechaElaboracion(LocalDate.now().minusDays(5)).fechaVencimiento(vence)
                .cantidad(cantidad).cantidadRestante(restante).build();
        masa.getExistencias().add(e);
        when(existenciaRepository.findById(100L)).thenReturn(Optional.of(e));
        return e;
    }

    @Test
    @DisplayName("Preparar 5 kg de masa descuenta 3 kg de harina y suma 5 kg de stock, con su costo")
    void crear_descuentaInsumosYSumaStock() {
        ExistenciaSemielaboradoResponse r = service.crear(30L,
                new ExistenciaSemielaboradoRequest(null, 5.0, LocalDate.now().plusDays(3)));

        assertThat(harina.getStockActual()).isEqualTo(7.0);
        assertThat(masa.getStockActual()).isEqualTo(5.0);
        assertThat(r.estado()).isEqualTo("VIGENTE");
        assertThat(r.costoUnitario()).isEqualByComparingTo("1200"); // 0,6 kg × $2000
        verify(manufacturadoRepository).save(masa);
    }

    @Test
    @DisplayName("Si no alcanza un insumo, rechaza sin descontar nada y dice qué falta")
    void crear_sinInsumosSuficientes_rechaza() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.crear(30L,
                new ExistenciaSemielaboradoRequest(null, 20.0, LocalDate.now().plusDays(3))));

        assertThat(ex.getMessage()).contains("Harina 000").contains("12000").contains("10000");
        assertThat(harina.getStockActual()).isEqualTo(10.0);
        assertThat(masa.getExistencias()).isEmpty();
    }

    @Test
    @DisplayName("No descuenta stock de insumo que ya está reservado por pedidos")
    void crear_respetaReservasDelInsumo() {
        harina.setStockReservado(8.0);

        assertThrows(IllegalStateException.class, () -> service.crear(30L,
                new ExistenciaSemielaboradoRequest(null, 5.0, LocalDate.now().plusDays(3))));
    }

    @Test
    @DisplayName("Vencimiento anterior a la elaboración es rechazado")
    void crear_vencimientoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> service.crear(30L,
                new ExistenciaSemielaboradoRequest(LocalDate.now(), 5.0, LocalDate.now().minusDays(1))));
    }

    @Test
    @DisplayName("Corregir un lote sin usar de 5 a 3 kg devuelve 1,2 kg de harina")
    void editar_achicar_devuelveInsumos() {
        lote(5.0, 5.0, LocalDate.now().plusDays(3));

        service.editar(100L, new ExistenciaSemielaboradoRequest(null, 3.0, LocalDate.now().plusDays(4)));

        assertThat(harina.getStockActual()).isEqualTo(11.2);
        assertThat(masa.getStockActual()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("Borrar un lote sin usar devuelve todos sus insumos")
    void eliminar_devuelveInsumos() {
        lote(5.0, 5.0, LocalDate.now().plusDays(3));

        service.eliminar(100L);

        assertThat(harina.getStockActual()).isEqualTo(13.0);
        assertThat(masa.getExistencias()).isEmpty();
        assertThat(masa.getStockActual()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Un lote que ya se usó en pedidos no se puede borrar ni corregir")
    void loteUsado_noSeModifica() {
        lote(5.0, 2.0, LocalDate.now().plusDays(3));

        assertThrows(IllegalStateException.class, () -> service.eliminar(100L));
        assertThrows(IllegalStateException.class,
                () -> service.editar(100L, new ExistenciaSemielaboradoRequest(null, 4.0, LocalDate.now().plusDays(3))));
        assertThat(harina.getStockActual()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("No deja borrar un lote si el stock que queda no cubre lo reservado por pedidos")
    void eliminar_conReservas_rechaza() {
        lote(5.0, 5.0, LocalDate.now().plusDays(3));
        masa.setStockReservado(2.0);

        assertThrows(IllegalStateException.class, () -> service.eliminar(100L));
        assertThat(harina.getStockActual()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Dar de baja: solo un lote vencido, y no devuelve insumos")
    void descartar() {
        ExistenciaSemielaborado vigente = lote(5.0, 5.0, LocalDate.now().plusDays(3));
        assertThrows(IllegalStateException.class, () -> service.descartar(100L));

        vigente.setFechaVencimiento(LocalDate.now().minusDays(1));
        ExistenciaSemielaboradoResponse r = service.descartar(100L);

        assertThat(r.estado()).isEqualTo("DESCARTADO");
        assertThat(harina.getStockActual()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Un producto que no es semielaborado no tiene lotes")
    void noSemielaborado_rechaza() {
        masa.setEsSemielaborado(false);

        assertThrows(IllegalArgumentException.class, () -> service.listar(30L));
        verify(manufacturadoRepository, never()).save(any());
    }
}
