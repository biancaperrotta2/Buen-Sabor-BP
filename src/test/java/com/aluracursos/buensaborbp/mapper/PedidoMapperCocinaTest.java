package com.aluracursos.buensaborbp.mapper;

import com.aluracursos.buensaborbp.dto.ItemCocinaResponse;
import com.aluracursos.buensaborbp.dto.PedidoCocinaResponse;
import com.aluracursos.buensaborbp.entity.ArticuloInsumo;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturado;
import com.aluracursos.buensaborbp.entity.DetallePedido;
import com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum;
import com.aluracursos.buensaborbp.entity.Pedido;
import com.aluracursos.buensaborbp.entity.Promocion;
import com.aluracursos.buensaborbp.entity.PromocionDetalle;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Lo que ve el cocinero en cada tarjeta: productos, combos desarmados y aclaraciones.
class PedidoMapperCocinaTest {

    private final PedidoMapper mapper = Mappers.getMapper(PedidoMapper.class);

    private static ArticuloManufacturado plato(String nombre, String preparacion) {
        return ArticuloManufacturado.builder()
                .nombre(nombre).precioVenta(BigDecimal.TEN)
                .unidadMedidaEnum(UnidadMedidaEnum.UNIDAD)
                .descripcion("desc").preparacion(preparacion).esSemielaborado(false)
                .build();
    }

    private static DetallePedido linea(Pedido pedido, com.aluracursos.buensaborbp.entity.Articulo articulo, int cantidad) {
        return DetallePedido.builder().pedido(pedido).articulo(articulo).cantidad(cantidad).subtotal(BigDecimal.ZERO).build();
    }

    @Test
    void armaLosItemsConPreparacionYDesarmaLasPromos() {
        ArticuloManufacturado hamburguesa = plato("Hamburguesa Cheese", "Plancha 4 min por lado");
        ArticuloManufacturado pizza = plato("Pizza Muzzarella", "  ");
        ArticuloInsumo gaseosa = ArticuloInsumo.builder()
                .nombre("Gaseosa").precioVenta(BigDecimal.ONE).unidadMedidaEnum(UnidadMedidaEnum.UNIDAD)
                .build();

        Promocion combo = Promocion.builder().nombre("Combo Familiar").precioVenta(BigDecimal.TEN)
                .unidadMedidaEnum(UnidadMedidaEnum.UNIDAD).promocionDetalle(new ArrayList<>()).build();
        combo.getPromocionDetalle().add(PromocionDetalle.builder().articulos(hamburguesa).cantidad(2).articuloPromocion(combo).build());
        combo.getPromocionDetalle().add(PromocionDetalle.builder().articulos(pizza).cantidad(1).articuloPromocion(combo).build());

        Pedido pedido = Pedido.builder().id(7L).aclaraciones("Sin cebolla").detallePedidos(new ArrayList<>()).build();
        pedido.getDetallePedidos().add(linea(pedido, gaseosa, 3));
        pedido.getDetallePedidos().add(linea(pedido, combo, 2));

        PedidoCocinaResponse respuesta = mapper.toCocinaResponse(pedido);

        assertThat(respuesta.aclaraciones()).isEqualTo("Sin cebolla");
        assertThat(respuesta.items()).hasSize(2);

        ItemCocinaResponse bebida = respuesta.items().get(0);
        assertThat(bebida.nombre()).isEqualTo("Gaseosa");
        assertThat(bebida.preparacion()).isNull();
        assertThat(bebida.incluye()).isEmpty();

        ItemCocinaResponse itemCombo = respuesta.items().get(1);
        assertThat(itemCombo.cantidad()).isEqualTo(2);
        // 2 combos: las cantidades de adentro se multiplican.
        assertThat(itemCombo.incluye()).extracting(ItemCocinaResponse::nombre, ItemCocinaResponse::cantidad, ItemCocinaResponse::preparacion)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("Hamburguesa Cheese", 4, "Plancha 4 min por lado"),
                        org.assertj.core.groups.Tuple.tuple("Pizza Muzzarella", 2, null));
    }

    @Test
    void pedidoSinLineasDevuelveListaVacia() {
        Pedido pedido = Pedido.builder().id(1L).detallePedidos(new ArrayList<>()).build();
        assertThat(mapper.toCocinaResponse(pedido).items()).isEqualTo(List.of());
    }
}
