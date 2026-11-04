package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.Agrupacion;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoBaseResponse;
import com.aluracursos.buensaborbp.dto.ArticuloStockResponse;
import com.aluracursos.buensaborbp.dto.CategoriaStock;
import com.aluracursos.buensaborbp.dto.IngresoPeriodoResponse;
import com.aluracursos.buensaborbp.dto.NivelStock;
import com.aluracursos.buensaborbp.dto.OrdenesPorClienteResponse;
import com.aluracursos.buensaborbp.dto.RankingProductoResponse;
import com.aluracursos.buensaborbp.dto.ResumenIngresosResponse;
import com.aluracursos.buensaborbp.entity.ArticuloInsumo;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturado;
import com.aluracursos.buensaborbp.entity.Cliente;
import com.aluracursos.buensaborbp.entity.DetallePedido;
import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.entity.Pedido;
import com.aluracursos.buensaborbp.mapper.ArticuloInsumoMapper;
import com.aluracursos.buensaborbp.repository.ArticuloInsumoRepository;
import com.aluracursos.buensaborbp.repository.ArticuloManufacturadoRepository;
import com.aluracursos.buensaborbp.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ReporteServiceImpl implements ReporteService {

    private static final DateTimeFormatter FORMATO_DIARIO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_MENSUAL = DateTimeFormatter.ofPattern("yyyy-MM");

    private final PedidoRepository pedidoRepository;
    private final ArticuloInsumoRepository articuloInsumoRepository;
    private final ArticuloManufacturadoRepository articuloManufacturadoRepository;
    private final ArticuloInsumoMapper articuloInsumoMapper;

    public ReporteServiceImpl(PedidoRepository pedidoRepository,
                               ArticuloInsumoRepository articuloInsumoRepository,
                               ArticuloManufacturadoRepository articuloManufacturadoRepository,
                               ArticuloInsumoMapper articuloInsumoMapper) {
        this.pedidoRepository = pedidoRepository;
        this.articuloInsumoRepository = articuloInsumoRepository;
        this.articuloManufacturadoRepository = articuloManufacturadoRepository;
        this.articuloInsumoMapper = articuloInsumoMapper;
    }

    private record Acumulador(String etiqueta, long cantidad) {
        Acumulador sumar(long extra) {
            return new Acumulador(etiqueta, cantidad + extra);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RankingProductoResponse> rankingProductos(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        List<Pedido> pedidos = pedidoRepository.findByFechaPedidoBetweenAndEstadoNot(desde, hasta, Estado.CANCELADO);

        Map<Long, Acumulador> acumulado = new HashMap<>();
        for (Pedido pedido : pedidos) {
            for (DetallePedido detalle : pedido.getDetallePedidos()) {
                if (detalle.getArticulo() == null) {
                    continue;
                }
                Long idArticulo = detalle.getArticulo().getId();
                acumulado.merge(idArticulo,
                        new Acumulador(detalle.getArticulo().getNombre(), detalle.getCantidad()),
                        (actual, nuevo) -> actual.sumar(nuevo.cantidad()));
            }
        }

        return acumulado.entrySet().stream()
                .map(e -> new RankingProductoResponse(e.getKey(), e.getValue().etiqueta(), e.getValue().cantidad()))
                .sorted(Comparator.comparingLong(RankingProductoResponse::cantidadVendida).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IngresoPeriodoResponse> ingresosPorPeriodo(LocalDate desde, LocalDate hasta, Agrupacion agrupacion) {
        validarRango(desde, hasta);
        List<Pedido> pedidos = pedidoRepository.findByFechaPedidoBetweenAndEstado(desde, hasta, Estado.ENTREGADO);

        DateTimeFormatter formato = agrupacion == Agrupacion.MENSUAL ? FORMATO_MENSUAL : FORMATO_DIARIO;
        Map<String, BigDecimal> totalPorPeriodo = new TreeMap<>();

        for (Pedido pedido : pedidos) {
            String periodo = pedido.getFechaPedido().format(formato);
            BigDecimal total = pedido.getTotal() != null ? pedido.getTotal() : BigDecimal.ZERO;
            totalPorPeriodo.merge(periodo, total, BigDecimal::add);
        }

        return totalPorPeriodo.entrySet().stream()
                .map(e -> new IngresoPeriodoResponse(e.getKey(), e.getValue()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenesPorClienteResponse> ordenesPorCliente(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        List<Pedido> pedidos = pedidoRepository.findByFechaPedidoBetweenAndEstadoNot(desde, hasta, Estado.CANCELADO);

        Map<Long, Acumulador> acumulado = new HashMap<>();
        for (Pedido pedido : pedidos) {
            Cliente cliente = pedido.getCliente();
            if (cliente == null) {
                continue;
            }
            Long idCliente = cliente.getId_usuario();
            String nombreCompleto = cliente.getNombre() + " " + cliente.getApellido();
            acumulado.merge(idCliente,
                    new Acumulador(nombreCompleto, 1),
                    (actual, nuevo) -> actual.sumar(1));
        }

        return acumulado.entrySet().stream()
                .map(e -> new OrdenesPorClienteResponse(e.getKey(), e.getValue().etiqueta(), e.getValue().cantidad()))
                .sorted(Comparator.comparingLong(OrdenesPorClienteResponse::cantidadOrdenes).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoBaseResponse> stockBajoMinimo() {
        return articuloInsumoMapper.toBaseResponseList(articuloInsumoRepository.findBajoStock());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloStockResponse> stockPorNivel(CategoriaStock categoria, NivelStock nivel) {
        List<ArticuloStockResponse> articulos = categoria == CategoriaStock.INSUMOS
                ? articuloInsumoRepository.findByActivoTrue().stream()
                        .map(this::aArticuloStockResponse)
                        .toList()
                : articuloManufacturadoRepository.findByActivoTrueAndEsSemielaboradoTrue().stream()
                        .map(this::aArticuloStockResponse)
                        .toList();

        return articulos.stream().filter(a -> a.nivel() == nivel).toList();
    }

    private ArticuloStockResponse aArticuloStockResponse(ArticuloInsumo insumo) {
        return new ArticuloStockResponse(insumo.getId(), insumo.getNombre(), insumo.getStockActual(),
                insumo.getStockMinimo(), insumo.getStockMaximo(),
                NivelStock.de(insumo.getStockActual(), insumo.getStockMinimo(), insumo.getStockMaximo()),
                CategoriaStock.INSUMOS);
    }

    private ArticuloStockResponse aArticuloStockResponse(ArticuloManufacturado semielaborado) {
        return new ArticuloStockResponse(semielaborado.getId(), semielaborado.getNombre(), semielaborado.getStockActual(),
                semielaborado.getStockMinimo(), semielaborado.getStockMaximo(),
                NivelStock.de(semielaborado.getStockActual(), semielaborado.getStockMinimo(), semielaborado.getStockMaximo()),
                CategoriaStock.SEMIELABORADOS);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenIngresosResponse resumenIngresos(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        List<Pedido> pedidos = pedidoRepository.findByFechaPedidoBetweenAndEstado(desde, hasta, Estado.ENTREGADO);

        BigDecimal total = pedidos.stream()
                .map(p -> p.getTotal() != null ? p.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ResumenIngresosResponse(pedidos.size(), total);
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
    }
}
