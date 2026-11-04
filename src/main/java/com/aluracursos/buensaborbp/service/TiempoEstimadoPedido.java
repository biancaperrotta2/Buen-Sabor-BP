package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.entity.Articulo;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturado;
import com.aluracursos.buensaborbp.entity.DetallePedido;
import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.entity.Enums.Rol;
import com.aluracursos.buensaborbp.entity.Enums.TipoEnvio;
import com.aluracursos.buensaborbp.entity.Pedido;
import com.aluracursos.buensaborbp.entity.Promocion;
import com.aluracursos.buensaborbp.repository.EmpleadoRepository;
import com.aluracursos.buensaborbp.repository.PedidoRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

// Fórmula del enunciado (sección 2):
//   Σ tiempo de cocción de cada manufacturado del pedido actual
// + Σ tiempo de los pedidos en cocina / cantidad de cocineros
// + 10 minutos si es delivery.
// Los tiempos se multiplican por la cantidad pedida; un insumo vendido directo (bebida) no
// suma; una promoción suma los manufacturados que incluye. "En cocina" son los mismos estados
// que muestra la pantalla de Cocina (EN_PROCESO y DEMORADO).
@Component
public class TiempoEstimadoPedido {

    static final int MINUTOS_DELIVERY = 10;
    private static final List<Estado> ESTADOS_EN_COCINA = List.of(Estado.EN_PROCESO, Estado.DEMORADO);

    private final PedidoRepository pedidoRepository;
    private final EmpleadoRepository empleadoRepository;

    public TiempoEstimadoPedido(PedidoRepository pedidoRepository, EmpleadoRepository empleadoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.empleadoRepository = empleadoRepository;
    }

    public LocalTime calcular(Pedido pedido, LocalTime desde) {
        return desde.plusMinutes(minutosTotales(pedido));
    }

    int minutosTotales(Pedido pedido) {
        int coccion = minutosCoccion(pedido);

        int enCocina = pedidoRepository.findByEstadoInOrderByHoraEstimadaFinalizacionAsc(ESTADOS_EN_COCINA).stream()
            .filter(otro -> !Objects.equals(otro.getId(), pedido.getId()))
            .mapToInt(TiempoEstimadoPedido::minutosCoccion)
            .sum();
        // Sin cocineros cargados se divide por 1 para no romper el cálculo.
        long cocineros = Math.max(1, empleadoRepository.contarActivosPorRol(Rol.COCINERO));
        int esperaCocina = (int) Math.ceil(enCocina / (double) cocineros);

        int delivery = pedido.getTipoEnvio() == TipoEnvio.DELIVERY ? MINUTOS_DELIVERY : 0;

        return coccion + esperaCocina + delivery;
    }

    static int minutosCoccion(Pedido pedido) {
        return pedido.getDetallePedidos().stream()
            .mapToInt(TiempoEstimadoPedido::minutosCoccion)
            .sum();
    }

    private static int minutosCoccion(DetallePedido detalle) {
        return minutosArticulo(detalle.getArticulo()) * detalle.getCantidad();
    }

    // DetallePedido.articulo es LAZY: sin unproxy el instanceof contra las subclases falla.
    private static int minutosArticulo(Articulo articuloLazy) {
        if (articuloLazy == null) {
            return 0;
        }
        Articulo articulo = (Articulo) Hibernate.unproxy(articuloLazy);
        if (articulo instanceof Promocion promocion) {
            return promocion.getPromocionDetalle().stream()
                .mapToInt(d -> minutosArticulo(d.getArticulos()) * d.getCantidad())
                .sum();
        }
        if (articulo instanceof ArticuloManufacturado && articulo.getTiempoEstimadoMinutos() != null) {
            return articulo.getTiempoEstimadoMinutos();
        }
        return 0;
    }
}
