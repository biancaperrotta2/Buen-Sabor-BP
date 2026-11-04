package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.entity.ArticuloManufacturado;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturadoDetalle;
import com.aluracursos.buensaborbp.repository.ArticuloManufacturadoDetalleRepository;
import com.aluracursos.buensaborbp.repository.ArticuloManufacturadoRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

// ArticuloManufacturado.precioCosto se guarda (lo leen tablas, ganancia y reportes), así que hay
// que recalcularlo cuando cambia el costo o la unidad de un ingrediente; si no, queda con el
// valor del día en que se guardó el producto. Si el producto recalculado es un semielaborado,
// sigue hacia los productos que lo usan. No toca promociones ni el costo de pedidos ya hechos
// (decisión del usuario: un pedido conserva el costo del momento en que se vendió).
@Component
public class RecalculoCostoProductos {

    private final ArticuloManufacturadoDetalleRepository detalleRepository;
    private final ArticuloManufacturadoRepository manufacturadoRepository;

    public RecalculoCostoProductos(ArticuloManufacturadoDetalleRepository detalleRepository,
                                   ArticuloManufacturadoRepository manufacturadoRepository) {
        this.detalleRepository = detalleRepository;
        this.manufacturadoRepository = manufacturadoRepository;
    }

    public void recalcularProductosQueUsan(Long articuloId) {
        recalcular(articuloId, new HashSet<>());
    }

    private void recalcular(Long componenteId, Set<Long> visitados) {
        if (!visitados.add(componenteId)) return;

        Map<Long, ArticuloManufacturado> productos = new LinkedHashMap<>();
        for (ArticuloManufacturadoDetalle detalle : detalleRepository.findByComponente_Id(componenteId)) {
            ArticuloManufacturado producto = detalle.getArticuloManufacturado();
            productos.putIfAbsent(producto.getId(), producto);
        }

        for (ArticuloManufacturado producto : productos.values()) {
            producto.calcularPrecioCosto();
            manufacturadoRepository.save(producto);
            if (Boolean.TRUE.equals(producto.getEsSemielaborado())) {
                recalcular(producto.getId(), visitados);
            }
        }
    }
}
