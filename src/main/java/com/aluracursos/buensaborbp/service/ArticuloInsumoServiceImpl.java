package com.aluracursos.buensaborbp.service;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoBaseResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoFullResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoRequest;
import com.aluracursos.buensaborbp.dto.PageResponse;
import com.aluracursos.buensaborbp.dto.UsoInsumoResponse;
import com.aluracursos.buensaborbp.dto.InsumoMenuResponse;
import com.aluracursos.buensaborbp.entity.ArticuloInsumo;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturadoDetalle;
import com.aluracursos.buensaborbp.entity.Categoria;
import com.aluracursos.buensaborbp.entity.Promocion;
import com.aluracursos.buensaborbp.repository.ArticuloManufacturadoDetalleRepository;
import com.aluracursos.buensaborbp.repository.PromocionRepository;
import com.aluracursos.buensaborbp.entity.Enums.TipoCategoria;
import com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum;
import com.aluracursos.buensaborbp.mapper.ArticuloInsumoMapper;
import com.aluracursos.buensaborbp.repository.ArticuloInsumoRepository;
import com.aluracursos.buensaborbp.repository.CategoriaRepository;
import org.springframework.data.domain.Pageable;

@Service
@Transactional
public class ArticuloInsumoServiceImpl extends BaseServiceImpl<ArticuloInsumo, Long> implements ArticuloInsumoService {

    private final ArticuloInsumoRepository articuloInsumoRepository;
    private final ArticuloInsumoMapper articuloInsumoMapper;
    private final CategoriaRepository categoriaRepository;
    private final ArticuloManufacturadoDetalleRepository detalleRepository;
    private final PromocionRepository promocionRepository;
    private final RecalculoCostoProductos recalculoCostoProductos;
    private final ImagenesArticulo imagenesArticulo;

    public ArticuloInsumoServiceImpl(
            ArticuloInsumoRepository articuloInsumoRepository,
            ArticuloInsumoMapper articuloInsumoMapper,
            CategoriaRepository categoriaRepository,
            ArticuloManufacturadoDetalleRepository detalleRepository,
            PromocionRepository promocionRepository,
            RecalculoCostoProductos recalculoCostoProductos,
            ImagenesArticulo imagenesArticulo) {
        super(articuloInsumoRepository);
        this.articuloInsumoRepository = articuloInsumoRepository;
        this.articuloInsumoMapper = articuloInsumoMapper;
        this.categoriaRepository = categoriaRepository;
        this.detalleRepository = detalleRepository;
        this.promocionRepository = promocionRepository;
        this.recalculoCostoProductos = recalculoCostoProductos;
        this.imagenesArticulo = imagenesArticulo;
    }

    // Para elaborar -> categoría de Insumos; se vende directo -> categoría de Productos (es donde
    // aparece en el menú). Se chequea con el estado final (categoría y tilde ya aplicados).
    private void validarTipoDeCategoria(ArticuloInsumo insumo) {
        if (insumo.getCategoria() == null) return;
        boolean seVende = Boolean.TRUE.equals(insumo.getEsVendible());
        insumo.getCategoria().exigirTipo(
                seVende ? TipoCategoria.PRODUCTO : TipoCategoria.INSUMO,
                seVende ? "un insumo que se vende directo" : "un insumo para elaborar");
    }

    @Override
    public List<InsumoMenuResponse> getVendibles() {
        return articuloInsumoRepository.findByActivoTrueAndEsVendibleTrue().stream()
                .map(articuloInsumoMapper::toMenuResponse)
                .toList();
    }

    @Override
    public UsoInsumoResponse obtenerUso(Long id) {
        if (!articuloInsumoRepository.existsById(id)) {
            throw new EntityNotFoundException("No se encontró el insumo con ID: " + id);
        }
        List<UsoInsumoResponse.RecetaQueLoUsa> recetas = detalleRepository.findByComponente_Id(id).stream()
                .map(d -> new UsoInsumoResponse.RecetaQueLoUsa(
                        d.getArticuloManufacturado().getId(), d.getArticuloManufacturado().getNombre(), d.getCantidad()))
                .toList();
        List<String> promociones = promocionRepository.findQueIncluyenArticulo(id).stream()
                .map(Promocion::getNombre)
                .toList();
        return new UsoInsumoResponse(recetas, promociones);
    }

    // Una receta guarda solo el número; la unidad la pone el insumo. Si la unidad cambia, sin
    // esto "250" (gramos) pasaría a leerse como 250 kilos en toda receta que lo use. Se
    // convierte cada cantidad para que siga representando lo mismo (250 g -> 0,25 kg). Las
    // promociones usan cantidades enteras (1 botella), que no se pueden convertir: ahí se rechaza.
    private void ajustarRecetasPorCambioDeUnidad(ArticuloInsumo insumo, UnidadMedidaEnum anterior, UnidadMedidaEnum nueva) {
        if (anterior == null || nueva == null || anterior.getFactor() == nueva.getFactor()) {
            return;
        }
        List<String> promociones = promocionRepository.findQueIncluyenArticulo(insumo.getId()).stream()
                .map(Promocion::getNombre)
                .toList();
        if (!promociones.isEmpty()) {
            throw new IllegalStateException("No se puede cambiar la unidad de \"" + insumo.getNombre()
                    + "\" porque está incluido directamente en las promociones: " + String.join(", ", promociones)
                    + ". Quitalo de esas promociones primero.");
        }
        double proporcion = anterior.getFactor() / nueva.getFactor();
        for (ArticuloManufacturadoDetalle detalle : detalleRepository.findByComponente_Id(insumo.getId())) {
            detalle.setCantidad(Math.round(detalle.getCantidad() * proporcion * 1_000_000d) / 1_000_000d);
            detalleRepository.save(detalle);
        }
    }

    // --- MÉTODOS DE STOCK (Los puentes a tu Entidad) ---

    @Override
    public void reservarStock(Long id, double cantidad, String articuloNombre) {
        ArticuloInsumo insumo = articuloInsumoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el insumo con ID: " + id));

        insumo.reservarStock(cantidad, articuloNombre); // Llama a tu método en la entidad
        articuloInsumoRepository.save(insumo); // Persiste el cambio en la DB
    }

    @Override
    public void liberarStock(Long id, double cantidad) {
        ArticuloInsumo insumo = articuloInsumoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el insumo con ID: " + id));

        insumo.liberarStock(cantidad);
        articuloInsumoRepository.save(insumo);
    }

    @Override
    public void confirmarVenta(Long id, double cantidad) {
        ArticuloInsumo insumo = articuloInsumoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el insumo con ID: " + id));

        insumo.confirmarStock(cantidad);
        articuloInsumoRepository.save(insumo);
    }

    // --- MÉTODOS CRUD ESTÁNDAR ---

    @Override
    public PageResponse<ArticuloInsumoBaseResponse> getAll(String busqueda, Pageable pageable) {
        String filtro = busqueda != null ? busqueda.trim() : "";
        return PageResponse.de(articuloInsumoRepository.buscar(filtro, pageable), articuloInsumoMapper::toBaseResponse);
    }

    @Override
    public ArticuloInsumoFullResponse getById(Long id) {
        ArticuloInsumo entity = articuloInsumoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el insumo con ID: " + id));
        return articuloInsumoMapper.toFullResponse(entity);
    }

    @Override
    public ArticuloInsumoFullResponse crearArticuloInsumo(ArticuloInsumoRequest request) {
        if (request.categoria() == null || request.categoria().idCategoriaPadre() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        if (request.unidadMedida() == null) {
            throw new IllegalArgumentException("La unidad de medida es obligatoria");
        }

        Categoria categoria = categoriaRepository.findById(request.categoria().idCategoriaPadre())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        ArticuloInsumo insumo = articuloInsumoMapper.toEntity(request);
        insumo.setCategoria(categoria);
        validarTipoDeCategoria(insumo);

        // El stock se ingresa (y se muestra) siempre en la unidad de medida elegida (ej.
        // Gramos), pero se almacena en la unidad base (KG/L/UNIDAD) como el resto de la
        // cascada de stock espera — ver Articulo.convertirACantidadBase/DesdeCantidadBase.
        insumo.setStockActual(insumo.convertirACantidadBase(request.stockActual()));
        insumo.setStockMinimo(insumo.convertirACantidadBase(request.stockMinimo()));
        insumo.setStockMaximo(insumo.convertirACantidadBase(request.stockMaximo()));

        if (request.precioVenta() != null) {
            insumo.calcularPrecioVenta(request.precioVenta());
        }

        // Fotos al final: sincronizar puede borrar archivos de Cloudinary (irreversible), así que
        // va después de todas las validaciones que pueden rechazar el guardado.
        imagenesArticulo.sincronizar(insumo, request.imagenes());
        return articuloInsumoMapper.toFullResponse(articuloInsumoRepository.save(insumo));
    }

    @Override
    public ArticuloInsumoFullResponse update(Long id, ArticuloInsumoRequest request) {
        ArticuloInsumo existing = articuloInsumoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el insumo con ID: " + id));

        if (request.categoria() != null && request.categoria().idCategoriaPadre() != null) {
            Categoria categoria = categoriaRepository.findById(request.categoria().idCategoriaPadre())
                    .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
            existing.setCategoria(categoria);
        }

        existing.setNombre(request.denominacion());
        // "¿Es para elaborar?" se puede cambiar al editar: destildado = se vende directo en el menú.
        existing.setEsVendible(!Boolean.TRUE.equals(request.esParaElaborar()));
        validarTipoDeCategoria(existing);
        existing.setPrecioCosto(request.precioCompra());
        existing.setMargen(request.margen());

        // La unidad se resuelve primero: el stock que llega en el request se interpreta en
        // ESA unidad (la que el admin tiene seleccionada ahora mismo en el formulario), no en
        // la que el insumo tenía antes.
        if (request.unidadMedida() != null) {
            UnidadMedidaEnum anterior = existing.getUnidadMedidaEnum();
            UnidadMedidaEnum nueva = UnidadMedidaEnum.desdeCatalogo(
                    request.unidadMedida().denominacion(), request.unidadMedida().abreviatura());
            ajustarRecetasPorCambioDeUnidad(existing, anterior, nueva);
            existing.setUnidadMedidaEnum(nueva);
        }
        existing.setStockActual(existing.convertirACantidadBase(request.stockActual()));
        existing.setStockMinimo(existing.convertirACantidadBase(request.stockMinimo()));
        existing.setStockMaximo(existing.convertirACantidadBase(request.stockMaximo()));

        if (request.precioVenta() != null) {
            existing.calcularPrecioVenta(request.precioVenta());
        }

        imagenesArticulo.sincronizar(existing, request.imagenes());
        ArticuloInsumo guardado = articuloInsumoRepository.save(existing);
        recalculoCostoProductos.recalcularProductosQueUsan(guardado.getId());
        return articuloInsumoMapper.toFullResponse(guardado);
    }

    @Override
    public void delete(Long id) {
        if (!articuloInsumoRepository.existsById(id)) {
            throw new EntityNotFoundException("No se encontró el insumo con ID: " + id);
        }
        articuloInsumoRepository.deleteById(id);
    }

    @Override
    public List<ArticuloInsumoBaseResponse> getActivos() {
        return articuloInsumoRepository.findByActivoTrue()
                .stream()
                .map(articuloInsumoMapper::toBaseResponse)
                .collect(Collectors.toList());
    }
}
