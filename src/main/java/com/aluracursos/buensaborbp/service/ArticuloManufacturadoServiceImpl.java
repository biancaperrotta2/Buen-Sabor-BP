package com.aluracursos.buensaborbp.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoDetalleRequest;
import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoFullResponse;
import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoBaseResponse;
import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoRequest;
import com.aluracursos.buensaborbp.entity.Articulo;
import com.aluracursos.buensaborbp.entity.ArticuloInsumo;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturado;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturadoDetalle;
import com.aluracursos.buensaborbp.entity.Categoria;
import com.aluracursos.buensaborbp.mapper.ArticuloManufacturadoMapper;
import com.aluracursos.buensaborbp.repository.ArticuloManufacturadoRepository;
import com.aluracursos.buensaborbp.repository.ArticuloRepository;
import com.aluracursos.buensaborbp.repository.CategoriaRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ArticuloManufacturadoServiceImpl extends BaseServiceImpl<ArticuloManufacturado, Long> implements ArticuloManufacturadoService {

    private final ArticuloManufacturadoRepository manufacturadoRepository;
    private final ArticuloManufacturadoMapper manufacturadoMapper;
    private final CategoriaRepository categoriaRepository;
    private final ArticuloRepository articuloRepository;

    public ArticuloManufacturadoServiceImpl(
            ArticuloManufacturadoRepository manufacturadoRepository,
            ArticuloManufacturadoMapper manufacturadoMapper,
            CategoriaRepository categoriaRepository,
            ArticuloRepository articuloRepository) {
        super(manufacturadoRepository);
        this.manufacturadoRepository = manufacturadoRepository;
        this.manufacturadoMapper = manufacturadoMapper;
        this.categoriaRepository = categoriaRepository;
        this.articuloRepository = articuloRepository;
    }

    /**
     * MÉTODO CLAVE PARA EL ECOMMERCE:
     * Verifica si hay ingredientes suficientes para preparar una cantidad X de un plato.
     */
    @Override
    public Boolean verificarStockParaVenta(Long manufacturadoId, Integer cantidadDeseada) {
        // Buscamos el plato con su receta (detalles)
        ArticuloManufacturado plato = manufacturadoRepository.findById(manufacturadoId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el plato ID: " + manufacturadoId));

        // Recorremos la receta; cada componente puede ser un insumo o un semielaborado,
        // ambos con stock propio (a diferencia de un plato final, que nunca lo tiene).
        return plato.getArticuloManufacturadoDetalles().stream().allMatch(detalle -> {
            double cantidadNecesaria = detalle.getCantidad() * cantidadDeseada;
            Articulo componente = detalle.getComponente();

            if (componente instanceof ArticuloInsumo insumo) {
                return insumo.getStockDisponible() >= insumo.convertirACantidadBase(cantidadNecesaria);
            }
            if (componente instanceof ArticuloManufacturado semielaborado) {
                return semielaborado.getStockDisponible() >= semielaborado.convertirACantidadBase(cantidadNecesaria);
            }
            return false;
        });
    }

    private List<ArticuloManufacturadoDetalle> construirDetalles(
            List<ArticuloManufacturadoDetalleRequest> detallesRequest, ArticuloManufacturado manufacturado) {
        List<ArticuloManufacturadoDetalle> detalles = new ArrayList<>();
        if (detallesRequest == null) {
            return detalles;
        }
        detallesRequest.forEach(d -> {
            Articulo componente = articuloRepository.findById(d.idComponente())
                    .orElseThrow(() -> new EntityNotFoundException("No se encontró el artículo con ID: " + d.idComponente()));

            boolean esInsumo = componente instanceof ArticuloInsumo;
            boolean esSemielaboradoValido = componente instanceof ArticuloManufacturado m && Boolean.TRUE.equals(m.getEsSemielaborado());
            if (!esInsumo && !esSemielaboradoValido) {
                throw new IllegalArgumentException("El artículo '" + componente.getNombre()
                        + "' no puede usarse como ingrediente: debe ser un insumo o un producto marcado como semielaborado");
            }

            detalles.add(ArticuloManufacturadoDetalle.builder()
                    .cantidad(d.cantidad())
                    .componente(componente)
                    .articuloManufacturado(manufacturado)
                    .build());
        });
        return detalles;
    }

    // esSemielaborado=true: necesita stock propio (mínimo y máximo obligatorios, actual
    // arranca en 0 si no se manda). esSemielaborado=false: nunca tiene stock propio.
    private void aplicarConfiguracionStock(ArticuloManufacturado manufacturado, ArticuloManufacturadoRequest request) {
        boolean esSemielaborado = Boolean.TRUE.equals(request.esSemielaborado());
        manufacturado.setEsSemielaborado(esSemielaborado);
        // Un semielaborado es un insumo interno para otras recetas, no algo que el cliente
        // pueda pedir directamente (mismo criterio que ArticuloInsumo.esParaElaborar).
        manufacturado.setEsVendible(!esSemielaborado);

        if (esSemielaborado) {
            if (request.stockMinimo() == null || request.stockMaximo() == null) {
                throw new IllegalArgumentException("Un semielaborado necesita stock mínimo y máximo");
            }
            // Si no viene stockActual en el request, no lo resetea: preserva el valor que ya
            // tenía (importante en un update que solo toca precio/descripción, por ejemplo).
            if (request.stockActual() != null) {
                manufacturado.setStockActual(request.stockActual());
            } else if (manufacturado.getStockActual() == null) {
                manufacturado.setStockActual(0.0);
            }
            manufacturado.setStockMinimo(request.stockMinimo());
            manufacturado.setStockMaximo(request.stockMaximo());
        } else {
            manufacturado.setStockActual(null);
            manufacturado.setStockMinimo(null);
            manufacturado.setStockMaximo(null);
        }
    }

    // Un plato final siempre se vende por unidad (no se compra/vende fraccionado, a
    // diferencia de un insumo). Un semielaborado sí puede pesarse/medirse en KG/GR/etc.
    private com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum unidadMedidaPara(ArticuloManufacturadoRequest request) {
        if (Boolean.TRUE.equals(request.esSemielaborado()) && request.unidadMedida() != null) {
            return request.unidadMedida();
        }
        return com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum.UNIDAD;
    }

    @Override
    @Transactional
    public ArticuloManufacturadoFullResponse crearManufacturado(ArticuloManufacturadoRequest manufacturadoRequest) {
        if (manufacturadoRequest.categoria() == null || manufacturadoRequest.categoria().idCategoriaPadre() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }

        Categoria categoria = categoriaRepository.findById(manufacturadoRequest.categoria().idCategoriaPadre())
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la categoría con ID: " + manufacturadoRequest.categoria().idCategoriaPadre()));

        ArticuloManufacturado manufacturado = manufacturadoMapper.toEntity(manufacturadoRequest);
        manufacturado.setCategoria(categoria);
        aplicarConfiguracionStock(manufacturado, manufacturadoRequest);
        manufacturado.setUnidadMedidaEnum(unidadMedidaPara(manufacturadoRequest));
        manufacturado.getArticuloManufacturadoDetalles()
                .addAll(construirDetalles(manufacturadoRequest.articuloManufacturadoDetalles(), manufacturado));

        manufacturado.calcularPrecioCosto();
        manufacturado.tiempoEstimadoMinutos(
                manufacturadoRequest.tiempoEstimado() == null ? 0 : manufacturadoRequest.tiempoEstimado());

        if (manufacturadoRequest.precioVenta() != null) {
            manufacturado.calcularPrecioVenta(manufacturadoRequest.precioVenta());
        }

        ArticuloManufacturado saved = manufacturadoRepository.save(manufacturado);
        return manufacturadoMapper.toFullResponse(saved);
    }

    @Override
    public Page<ArticuloManufacturadoBaseResponse> getAll() {
        Page<ArticuloManufacturado> page = manufacturadoRepository.findAllByEsSemielaboradoFalse(PageRequest.of(0, 20));
        return page.map(manufacturadoMapper::toBaseResponse);
    }

    @Override
    public Page<ArticuloManufacturadoBaseResponse> getActivos() {
        Page<ArticuloManufacturado> page = manufacturadoRepository.findAllByActivoTrueAndEsSemielaboradoFalse(PageRequest.of(0, 20));
        return page.map(manufacturadoMapper::toBaseResponse);
    }

    @Override
    public Page<ArticuloManufacturadoBaseResponse> getSemielaborados() {
        Page<ArticuloManufacturado> page = manufacturadoRepository.findAllByEsSemielaboradoTrue(PageRequest.of(0, 20));
        return page.map(manufacturadoMapper::toBaseResponse);
    }

    @Override
    public ArticuloManufacturadoFullResponse getById(Long id) {
        ArticuloManufacturado entity = manufacturadoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el manufacturado con ID: " + id));
        return manufacturadoMapper.toFullResponse(entity);
    }

    @Override
    @Transactional
    public ArticuloManufacturadoFullResponse update(Long id, ArticuloManufacturadoRequest request) {
        ArticuloManufacturado existing = manufacturadoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el manufacturado con ID: " + id));

        if (request.categoria() != null && request.categoria().idCategoriaPadre() != null) {
            Categoria categoria = categoriaRepository.findById(request.categoria().idCategoriaPadre())
                    .orElseThrow(() -> new EntityNotFoundException("No se encontró la categoría con ID: " + request.categoria().idCategoriaPadre()));
            existing.setCategoria(categoria);
        }

        existing.setNombre(request.denominacion());
        existing.setDescripcion(request.descripcion());
        existing.setPreparacion(request.preparacion());
        aplicarConfiguracionStock(existing, request);
        existing.setUnidadMedidaEnum(unidadMedidaPara(request));

        existing.getArticuloManufacturadoDetalles().clear();
        existing.getArticuloManufacturadoDetalles()
                .addAll(construirDetalles(request.articuloManufacturadoDetalles(), existing));

        existing.calcularPrecioCosto();
        existing.tiempoEstimadoMinutos(request.tiempoEstimado() == null ? 0 : request.tiempoEstimado());

        if (request.precioVenta() != null) {
            existing.calcularPrecioVenta(request.precioVenta());
        }

        ArticuloManufacturado saved = manufacturadoRepository.save(existing);
        return manufacturadoMapper.toFullResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!manufacturadoRepository.existsById(id)) {
            throw new EntityNotFoundException("No se encontró el manufacturado con ID: " + id);
        }
        manufacturadoRepository.deleteById(id);
    }
}
