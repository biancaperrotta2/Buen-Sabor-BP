package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.entity.Articulo;
import com.aluracursos.buensaborbp.entity.ArticuloInsumo;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturado;
import com.aluracursos.buensaborbp.entity.DetallePedido;
import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.entity.Enums.Rol;
import com.aluracursos.buensaborbp.entity.Enums.TipoEnvio;
import com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum;
import com.aluracursos.buensaborbp.entity.Pedido;
import com.aluracursos.buensaborbp.entity.Promocion;
import com.aluracursos.buensaborbp.entity.PromocionDetalle;
import com.aluracursos.buensaborbp.repository.EmpleadoRepository;
import com.aluracursos.buensaborbp.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TiempoEstimadoPedidoTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private EmpleadoRepository empleadoRepository;

    private TiempoEstimadoPedido tiempo;

    private final ArticuloManufacturado pizza = plato("Pizza", 15);
    private final ArticuloManufacturado hamburguesa = plato("Hamburguesa", 10);
    private final ArticuloInsumo gaseosa = ArticuloInsumo.builder()
            .nombre("Gaseosa").precioVenta(BigDecimal.ONE).unidadMedidaEnum(UnidadMedidaEnum.UNIDAD)
            .tiempoEstimadoMinutos(5) // un insumo no se cocina: no debe sumar aunque tenga tiempo
            .build();

    @BeforeEach
    void setUp() {
        tiempo = new TiempoEstimadoPedido(pedidoRepository, empleadoRepository);
    }

    private static ArticuloManufacturado plato(String nombre, int minutos) {
        return ArticuloManufacturado.builder()
                .nombre(nombre).precioVenta(BigDecimal.TEN).unidadMedidaEnum(UnidadMedidaEnum.UNIDAD)
                .tiempoEstimadoMinutos(minutos).esSemielaborado(false)
                .build();
    }

    private static Pedido pedido(Long id, TipoEnvio tipoEnvio, Object... articuloYCantidad) {
        Pedido pedido = Pedido.builder().id(id).tipoEnvio(tipoEnvio).estado(Estado.EN_PROCESO)
                .detallePedidos(new ArrayList<>()).build();
        for (int i = 0; i < articuloYCantidad.length; i += 2) {
            pedido.getDetallePedidos().add(DetallePedido.builder().pedido(pedido)
                    .articulo((Articulo) articuloYCantidad[i]).cantidad((Integer) articuloYCantidad[i + 1])
                    .subtotal(BigDecimal.ZERO).build());
        }
        return pedido;
    }

    private void enCocina(List<Pedido> pedidos, long cocineros) {
        when(pedidoRepository.findByEstadoInOrderByHoraEstimadaFinalizacionAsc(anyList())).thenReturn(pedidos);
        when(empleadoRepository.contarActivosPorRol(Rol.COCINERO)).thenReturn(cocineros);
    }

    @Test
    void sumaCoccionPorCantidadSinContarInsumos() {
        enCocina(List.of(), 1);
        Pedido actual = pedido(null, TipoEnvio.TAKE_AWAY, pizza, 3, gaseosa, 2);

        assertThat(tiempo.minutosTotales(actual)).isEqualTo(45);
    }

    @Test
    void sumaLaCocinaDivididaPorCocinerosYDelivery() {
        // En cocina: 2 pizzas (30) + 1 hamburguesa (10) = 40 min, entre 3 cocineros = 13,3 -> 14.
        enCocina(List.of(pedido(1L, TipoEnvio.TAKE_AWAY, pizza, 2), pedido(2L, TipoEnvio.DELIVERY, hamburguesa, 1)), 3);
        Pedido actual = pedido(null, TipoEnvio.DELIVERY, hamburguesa, 1);

        assertThat(tiempo.minutosTotales(actual)).isEqualTo(10 + 14 + 10);
        assertThat(tiempo.calcular(actual, LocalTime.of(20, 0))).isEqualTo(LocalTime.of(20, 34));
    }

    @Test
    void unaPromocionSumaLosManufacturadosQueIncluye() {
        enCocina(List.of(), 1);
        Promocion combo = Promocion.builder().nombre("Combo").precioVenta(BigDecimal.TEN)
                .unidadMedidaEnum(UnidadMedidaEnum.UNIDAD).promocionDetalle(new ArrayList<>()).build();
        combo.getPromocionDetalle().add(PromocionDetalle.builder().articulos(hamburguesa).cantidad(2).articuloPromocion(combo).build());
        combo.getPromocionDetalle().add(PromocionDetalle.builder().articulos(gaseosa).cantidad(2).articuloPromocion(combo).build());

        // 2 combos x (2 hamburguesas de 10) = 40.
        assertThat(tiempo.minutosTotales(pedido(null, TipoEnvio.TAKE_AWAY, combo, 2))).isEqualTo(40);
    }

    @Test
    void alAceptarNoSeCuentaASiMismoYSinCocinerosDividePorUno() {
        Pedido actual = pedido(5L, TipoEnvio.TAKE_AWAY, pizza, 1);
        enCocina(List.of(actual, pedido(6L, TipoEnvio.TAKE_AWAY, hamburguesa, 2)), 0);

        assertThat(tiempo.minutosTotales(actual)).isEqualTo(15 + 20);
    }
}
