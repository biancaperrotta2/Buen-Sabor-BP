package com.aluracursos.buensaborbp.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aluracursos.buensaborbp.dto.PromocionRequest;
import com.aluracursos.buensaborbp.entity.Categoria;
import com.aluracursos.buensaborbp.entity.Enums.TipoCategoria;
import com.aluracursos.buensaborbp.entity.ImagenPromocion;
import com.aluracursos.buensaborbp.entity.Promocion;
import com.aluracursos.buensaborbp.entity.Articulo;
import com.aluracursos.buensaborbp.entity.PromocionDetalle;
import com.aluracursos.buensaborbp.repository.CategoriaRepository;
import com.aluracursos.buensaborbp.repository.PromocionRepository;
import com.aluracursos.buensaborbp.repository.ArticuloRepository;
import com.aluracursos.buensaborbp.mapper.PromocionMapper;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.aluracursos.buensaborbp.dto.PageResponse;
import com.aluracursos.buensaborbp.dto.PromocionBaseResponse;
import com.aluracursos.buensaborbp.dto.PromocionFullResponse;
import java.util.ArrayList;

@Service
public class PromocionServiceImpl extends BaseServiceImpl<Promocion, Long> implements PromocionService {

    private final PromocionRepository promocionRepository;
    private final PromocionMapper promocionMapper;
    private final ArticuloRepository articuloRepository;
    private final CategoriaRepository categoriaRepository;
    private final CloudinaryService cloudinaryService;

    public PromocionServiceImpl(PromocionRepository promocionRepository,
                                PromocionMapper promocionMapper,
                                ArticuloRepository articuloRepository,
                                CategoriaRepository categoriaRepository,
                                CloudinaryService cloudinaryService) {
        super(promocionRepository);
        this.promocionRepository = promocionRepository;
        this.promocionMapper = promocionMapper;
        this.articuloRepository = articuloRepository;
        this.categoriaRepository = categoriaRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromocionBaseResponse> listarParaMenu() {
        return promocionMapper.toBaseResponseList(promocionRepository.findVigentes());
    }

    @Override
    @Transactional(readOnly = true)
    public PromocionFullResponse buscarPorIdFull(Long id) {
        Promocion p = promocionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Promoción no encontrada con ID: " + id));
        return promocionMapper.toFullResponse(p);
    }

    @Override
    @Transactional
    public PromocionFullResponse crearPromocion(PromocionRequest request) {
        Categoria categoria = categoriaRepository.findById(request.idCategoria())
            .orElseThrow(() -> new EntityNotFoundException("No se encontró la categoría con ID: " + request.idCategoria()));
        // Una promo se muestra en el menú: va en una categoría de Productos.
        categoria.exigirTipo(TipoCategoria.PRODUCTO, "una promoción");

        Promocion p = Promocion.builder()
            .nombre(request.nombre())
            .fechaDesde(request.fechaDesde())
            .fechaHasta(request.fechaHasta())
            .horaDesde(request.horaDesde())
            .horaHasta(request.horaHasta())
            .descripcionDescuento(request.descripcionDescuento())
            .tipoPromocion(request.tipoPromocion()) // Mapeo del Enum
            .categoria(categoria)
            // Una promo se vende "por unidad" (se pide 1 combo, no se pesa) — mismo default que
            // usa un producto final; el campo es obligatorio en la tabla base Articulo.
            .unidadMedidaEnum(com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum.UNIDAD)
            .promocionDetalle(new ArrayList<>())
            .build();

        agregarDetalles(p, request.detalles());

        // Aplicamos métodos de cálculo de la entidad
        p.calcularPrecioCosto();
        p.calcularPrecioTotal();
        p.calcularPrecioVenta(request.precioVenta());

        // Fotos al final: sincronizar puede borrar archivos de Cloudinary (irreversible).
        sincronizarImagenes(p, request.imagenesUrls());
        return promocionMapper.toFullResponse(promocionRepository.save(p));
    }

    @Override
    @Transactional
    public PromocionFullResponse actualizarPromocion(Long id, PromocionRequest request) {
        // Buscar la promoción existente
        Promocion promocion = promocionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Promoción no encontrada con ID: " + id));

        Categoria categoria = categoriaRepository.findById(request.idCategoria())
            .orElseThrow(() -> new EntityNotFoundException("No se encontró la categoría con ID: " + request.idCategoria()));
        // Una promo se muestra en el menú: va en una categoría de Productos.
        categoria.exigirTipo(TipoCategoria.PRODUCTO, "una promoción");

        // Actualizar campos principales
        promocion.setNombre(request.nombre());
        promocion.setDescripcionDescuento(request.descripcionDescuento());
        promocion.setFechaDesde(request.fechaDesde());
        promocion.setFechaHasta(request.fechaHasta());
        promocion.setHoraDesde(request.horaDesde());
        promocion.setHoraHasta(request.horaHasta());
        promocion.setTipoPromocion(request.tipoPromocion()); // Mapeo del Enum
        promocion.setCategoria(categoria);

        // Actualizar detalles: limpiar y agregar los nuevos detalles
        promocion.getPromocionDetalle().clear();
        agregarDetalles(promocion, request.detalles());

        // Aplicar lógica de recalculo de precios en la entidad
        promocion.calcularPrecioCosto();
        promocion.calcularPrecioTotal();
        promocion.calcularPrecioVenta(request.precioVenta());

        sincronizarImagenes(promocion, request.imagenesUrls());
        // Guardar cambios y devolver el dto
        promocionRepository.save(promocion);

        return promocionMapper.toFullResponse(promocion);
    }

    private void agregarDetalles(Promocion promocion, List<com.aluracursos.buensaborbp.dto.PromocionDetalleRequest> detalles) {
        if (detalles == null) return;
        detalles.forEach(d -> {
            Articulo art = articuloRepository.findById(d.idArticulo())
                .orElseThrow(() -> new EntityNotFoundException("Artículo no encontrado con ID: " + d.idArticulo()));

            PromocionDetalle detalle = PromocionDetalle.builder()
                .articulos(art)
                .cantidad(d.cantidad())
                .articuloPromocion(promocion)
                .build();

            promocion.getPromocionDetalle().add(detalle);
        });
    }

    // Mismo patrón que ArticuloManufacturadoServiceImpl.sincronizarImagenes: deja las fotos
    // igual a la lista pedida (agrega las nuevas, borra de Cloudinary las que ya no están).
    // null = no se tocan las fotos existentes (por si el form no las reenvía).
    private void sincronizarImagenes(Promocion promocion, List<String> urlsSolicitadas) {
        if (urlsSolicitadas == null) return;

        Set<String> urlsPedidas = new LinkedHashSet<>();
        for (String url : urlsSolicitadas) {
            if (url == null || url.isBlank()) continue;
            if (!url.startsWith("https://")) {
                throw new IllegalArgumentException("La URL de la imagen debe ser https");
            }
            urlsPedidas.add(url);
        }

        List<String> aEliminarDeCloudinary = promocion.getImagenesPromocion().stream()
                .map(ImagenPromocion::getUrlImagen)
                .filter(url -> !urlsPedidas.contains(url))
                .toList();
        promocion.getImagenesPromocion().removeIf(imagen -> !urlsPedidas.contains(imagen.getUrlImagen()));

        Set<String> yaGuardadas = new HashSet<>();
        promocion.getImagenesPromocion().forEach(imagen -> yaGuardadas.add(imagen.getUrlImagen()));
        for (String url : urlsPedidas) {
            if (yaGuardadas.contains(url)) continue;
            promocion.getImagenesPromocion().add(ImagenPromocion.builder()
                    .denominacion(promocion.getNombre())
                    .urlImagen(url)
                    .promocion(promocion)
                    .build());
        }

        aEliminarDeCloudinary.stream()
                .filter(url -> url.contains("res.cloudinary.com"))
                .forEach(cloudinaryService::eliminarImagen);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromocionFullResponse> listarTodoParaAdmin() {
        // Obtener todas las promociones (habilitadas y no habilitadas)
        List<Promocion> promociones = promocionRepository.findAll();

        // Mapear cada promoción a su DTO correspondiente
        return promociones.stream()
                .map(promocionMapper::toFullResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PromocionBaseResponse> listarAdmin(String busqueda, Pageable pageable) {
        String filtro = busqueda != null ? busqueda.trim() : "";
        return PageResponse.de(promocionRepository.buscar(filtro, pageable), promocionMapper::toBaseResponse);
    }

    @Override
    @Transactional
    public void cambiarEstadoHabilitacion(Long id) {
        // Buscar la promoción por su id
        Promocion promocion = promocionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Promoción no encontrada con ID: " + id));

        // Cambiar el estado de habilitación (baja lógica)
        promocion.setActivo(!Boolean.TRUE.equals(promocion.getActivo()));

        // Guardar cambios
        promocionRepository.save(promocion);
    }
}
