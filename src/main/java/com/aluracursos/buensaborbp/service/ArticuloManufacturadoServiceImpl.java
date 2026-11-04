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
import com.aluracursos.buensaborbp.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import com.aluracursos.buensaborbp.entity.Articulo;
import com.aluracursos.buensaborbp.entity.ArticuloInsumo;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturado;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturadoDetalle;
import com.aluracursos.buensaborbp.entity.Categoria;
import com.aluracursos.buensaborbp.entity.Enums.TipoCategoria;
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
    private final ImagenesArticulo imagenesArticulo;
    private final RecalculoCostoProductos recalculoCostoProductos;

    public ArticuloManufacturadoServiceImpl(
            ArticuloManufacturadoRepository manufacturadoRepository,
            ArticuloManufacturadoMapper manufacturadoMapper,
            CategoriaRepository categoriaRepository,
            ArticuloRepository articuloRepository,
            ImagenesArticulo imagenesArticulo,
            RecalculoCostoProductos recalculoCostoProductos) {
        super(manufacturadoRepository);
        this.manufacturadoRepository = manufacturadoRepository;
        this.manufacturadoMapper = manufacturadoMapper;
        this.categoriaRepository = categoriaRepository;
        this.articuloRepository = articuloRepository;
        this.imagenesArticulo = imagenesArticulo;
        this.recalculoCostoProductos = recalculoCostoProductos;
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
            // La receta de un semielaborado lleva solo insumos (enunciado #23): al cargar un lote
            // se descuentan de ellos.
            if (Boolean.TRUE.equals(manufacturado.getEsSemielaborado()) && !esInsumo) {
                throw new IllegalArgumentException("El ingrediente '" + componente.getNombre()
                        + "' no es un insumo: la receta de un semielaborado solo puede llevar insumos");
            }
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
            if (request.stockMinimo() > request.stockMaximo()) {
                throw new IllegalArgumentException("El stock mínimo no puede ser mayor que el máximo");
            }
            // El stock actual no se toma del request: es la suma de los lotes vigentes (se carga
            // con "Añadir existencia"). Un semielaborado nuevo arranca en 0.
            manufacturado.actualizarStockDesdeExistencias(java.time.LocalDate.now());
            if (manufacturado.getStockActual() == null) {
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
    // Un plato final necesita precio de venta (> 0) y preparación; un semielaborado no se vende,
    // así que se guarda con precio 0 y la preparación es opcional.
    private void aplicarPrecioYPreparacion(ArticuloManufacturado manufacturado, ArticuloManufacturadoRequest request) {
        if (Boolean.TRUE.equals(manufacturado.getEsSemielaborado())) {
            manufacturado.setPrecioVenta(java.math.BigDecimal.ZERO);
            manufacturado.setPreparacion(request.preparacion() != null ? request.preparacion() : "");
            return;
        }
        if (request.preparacion() == null || request.preparacion().isBlank()) {
            throw new IllegalArgumentException("La preparación es obligatoria");
        }
        manufacturado.setPreparacion(request.preparacion());
        manufacturado.calcularPrecioVenta(request.precioVenta());
    }

    private com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum unidadMedidaPara(ArticuloManufacturadoRequest request) {
        if (Boolean.TRUE.equals(request.esSemielaborado()) && request.unidadMedida() != null) {
            // Solo unidades base (Kilos, Litros, Unidad): su costo se calcula por 1 unidad de su
            // medida y, como ingrediente de un plato, se interpreta por unidad base. Con Gramos o
            // Mililitros esos dos números no coincidirían.
            if (request.unidadMedida().getFactor() != 1.0) {
                throw new IllegalArgumentException("Un semielaborado se mide en Kilos, Litros o Unidad (no en "
                        + request.unidadMedida().getDenominacion() + ")");
            }
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
        validarTipoDeCategoria(manufacturado);
        manufacturado.setUnidadMedidaEnum(unidadMedidaPara(manufacturadoRequest));
        manufacturado.getArticuloManufacturadoDetalles()
                .addAll(construirDetalles(manufacturadoRequest.articuloManufacturadoDetalles(), manufacturado));

        manufacturado.calcularPrecioCosto();
        manufacturado.tiempoEstimadoMinutos(
                manufacturadoRequest.tiempoEstimado() == null ? 0 : manufacturadoRequest.tiempoEstimado());

        aplicarPrecioYPreparacion(manufacturado, manufacturadoRequest);

        // Fotos al final: sincronizar puede borrar archivos de Cloudinary (irreversible), así que
        // va después de todo lo que puede rechazar el guardado (categoría, costo, precio).
        imagenesArticulo.sincronizar(manufacturado, manufacturadoRequest.imagenes());
        ArticuloManufacturado saved = manufacturadoRepository.save(manufacturado);
        return manufacturadoMapper.toFullResponse(saved);
    }

    // Plato final -> categoría de Productos (se ve en el menú); semielaborado -> categoría de
    // Insumos (es un ingrediente, no se vende solo).
    private void validarTipoDeCategoria(ArticuloManufacturado manufacturado) {
        if (manufacturado.getCategoria() == null) return;
        boolean semielaborado = Boolean.TRUE.equals(manufacturado.getEsSemielaborado());
        manufacturado.getCategoria().exigirTipo(
                semielaborado ? TipoCategoria.INSUMO : TipoCategoria.PRODUCTO,
                semielaborado ? "un semielaborado" : "un producto");
    }

    @Override
    public Page<ArticuloManufacturadoBaseResponse> getAll() {
        Page<ArticuloManufacturado> page = manufacturadoRepository.findAllByEsSemielaboradoFalse(PageRequest.of(0, 20));
        return page.map(manufacturadoMapper::toBaseResponse);
    }

    @Override
    public PageResponse<ArticuloManufacturadoBaseResponse> getAllAdmin(String busqueda, Pageable pageable) {
        String filtro = busqueda != null ? busqueda.trim() : "";
        return PageResponse.de(manufacturadoRepository.buscarProductos(filtro, pageable), manufacturadoMapper::toBaseResponse);
    }

    @Override
    public Page<ArticuloManufacturadoBaseResponse> getActivos() {
        Page<ArticuloManufacturado> page = manufacturadoRepository.findAllByActivoTrueAndEsSemielaboradoFalse(PageRequest.of(0, 20));
        return page.map(manufacturadoMapper::toBaseResponse);
    }

    // Tabla de semielaborados. Recalcula el stock de cada uno antes de mostrarlo, para que un lote
    // que venció desde la última vez deje de contar (y el semáforo de disponibilidad sea real).
    @Override
    @Transactional
    public PageResponse<com.aluracursos.buensaborbp.dto.SemielaboradoAdminResponse> getSemielaboradosAdmin(String busqueda, Pageable pageable) {
        String filtro = busqueda != null ? busqueda.trim() : "";
        java.time.LocalDate hoy = java.time.LocalDate.now();
        return PageResponse.de(manufacturadoRepository.buscarSemielaborados(filtro, pageable), semi -> {
            semi.actualizarStockDesdeExistencias(hoy);
            return manufacturadoMapper.toSemielaboradoAdmin(semi);
        });
    }

    @Override
    public Page<ArticuloManufacturadoBaseResponse> getSemielaborados() {
        Page<ArticuloManufacturado> page = manufacturadoRepository.findAllByEsSemielaboradoTrue(PageRequest.of(0, 20));
        return page.map(manufacturadoMapper::toBaseResponse);
    }

    @Override
    @Transactional
    public ArticuloManufacturadoFullResponse getById(Long id) {
        ArticuloManufacturado entity = manufacturadoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el manufacturado con ID: " + id));
        entity.actualizarStockDesdeExistencias(java.time.LocalDate.now());
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
        aplicarConfiguracionStock(existing, request);
        validarTipoDeCategoria(existing);
        existing.setUnidadMedidaEnum(unidadMedidaPara(request));

        // null = "no se tocó la receta": el formulario del admin todavía no edita ingredientes y
        // antes borraba la receta entera en cada edición.
        if (request.articuloManufacturadoDetalles() != null) {
            existing.getArticuloManufacturadoDetalles().clear();
            existing.getArticuloManufacturadoDetalles()
                    .addAll(construirDetalles(request.articuloManufacturadoDetalles(), existing));
        }

        existing.calcularPrecioCosto();
        existing.tiempoEstimadoMinutos(request.tiempoEstimado() == null ? 0 : request.tiempoEstimado());

        aplicarPrecioYPreparacion(existing, request);

        imagenesArticulo.sincronizar(existing, request.imagenes());
        ArticuloManufacturado saved = manufacturadoRepository.save(existing);
        // Un semielaborado es ingrediente de otros productos: su costo nuevo tiene que llegarles.
        if (Boolean.TRUE.equals(saved.getEsSemielaborado())) {
            recalculoCostoProductos.recalcularProductosQueUsan(saved.getId());
        }
        return manufacturadoMapper.toFullResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ArticuloManufacturado manufacturado = manufacturadoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el manufacturado con ID: " + id));
        // Borrar un semielaborado con lotes vigentes se llevaría puesto ese stock (y los insumos ya
        // descontados para prepararlo) sin dejar rastro.
        if (Boolean.TRUE.equals(manufacturado.getEsSemielaborado())) {
            manufacturado.actualizarStockDesdeExistencias(java.time.LocalDate.now());
            if (manufacturado.getStockActual() != null && manufacturado.getStockActual() > 0) {
                throw new IllegalStateException("\"" + manufacturado.getNombre() + "\" todavía tiene stock en lotes ("
                        + manufacturado.getStockActual() + "). Usalo, borrá los lotes o esperá a que venzan antes de eliminarlo.");
            }
        }
        manufacturadoRepository.delete(manufacturado);
    }
}
