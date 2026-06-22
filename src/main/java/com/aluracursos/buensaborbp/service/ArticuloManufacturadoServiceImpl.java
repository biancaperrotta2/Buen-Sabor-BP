package com.aluracursos.buensaborbp.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoFullResponse;
import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoBaseResponse;
import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoRequest;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturado;
import com.aluracursos.buensaborbp.entity.Categoria;
import com.aluracursos.buensaborbp.mapper.ArticuloManufacturadoMapper;
import com.aluracursos.buensaborbp.repository.ArticuloManufacturadoRepository;
import com.aluracursos.buensaborbp.repository.CategoriaRepository;

@Service
@Transactional
public class ArticuloManufacturadoServiceImpl extends BaseServiceImpl<ArticuloManufacturado, Long> implements ArticuloManufacturadoService {

    private final ArticuloManufacturadoRepository manufacturadoRepository;
    private final ArticuloManufacturadoMapper manufacturadoMapper;
    private final CategoriaRepository categoriaRepository;
    private final ArticuloInsumoService articuloInsumoService;

    public ArticuloManufacturadoServiceImpl(
            ArticuloManufacturadoRepository manufacturadoRepository,
            ArticuloManufacturadoMapper manufacturadoMapper,
            CategoriaRepository categoriaRepository,
            ArticuloInsumoService articuloInsumoService) { // Lo sumamos al constructor
        super(manufacturadoRepository);
        this.manufacturadoRepository = manufacturadoRepository;
        this.manufacturadoMapper = manufacturadoMapper;
        this.categoriaRepository = categoriaRepository;
        this.articuloInsumoService = articuloInsumoService;
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

        // Recorremos la receta
        return plato.getArticuloManufacturadoDetalles().stream().allMatch(detalle -> {
            // Calculamos cuánto necesitamos de este insumo: (cantidad de la receta * cantidad de platos)
            double cantidadNecesaria = detalle.getCantidad() * cantidadDeseada;
            
            // Le preguntamos al service de insumos si tiene stock disponible
            // Nota: getById debe devolver el Response, así que comparamos contra el stockDisponible del DTO
            var insumoStatus = articuloInsumoService.getById(detalle.getArticuloInsumo().getId());
            return insumoStatus.getStockActual() - insumoStatus.getStockReservado() >= cantidadNecesaria;
        });
    }

    @Override 
    @Transactional
    public ArticuloManufacturadoFullResponse crearManufacturado (ArticuloManufacturadoRequest manufacturadoRequest){
        if (manufacturadoRequest.getCategoria() == null || manufacturadoRequest.getCategoria().getIdCategoriaPadre() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        
        Categoria categoria = categoriaRepository.findById(manufacturadoRequest.getCategoria().getIdCategoriaPadre())
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la categoría con ID: " + manufacturadoRequest.getCategoria().getIdCategoriaPadre()));
        
        ArticuloManufacturado manufacturado = manufacturadoMapper.toEntity(manufacturadoRequest);
        manufacturado.setCategoria(categoria);
        
        if (manufacturadoRequest.getPrecioVenta() != null) {
            manufacturado.calcularPrecioVenta(manufacturadoRequest.getPrecioVenta());
        }
        
        ArticuloManufacturado saved = manufacturadoRepository.save(manufacturado);
        return manufacturadoMapper.toFullResponse(saved);
    }

    @Override
    public Page<ArticuloManufacturadoBaseResponse> getAll() {
        Page<ArticuloManufacturado> page = manufacturadoRepository.findAll(PageRequest.of(0, 20));
        return page.map(manufacturadoMapper::toBaseResponse);
    }

    @Override
    public Page<ArticuloManufacturadoBaseResponse> getActivos() {
        Page<ArticuloManufacturado> page = manufacturadoRepository.findAllByActivoTrue(PageRequest.of(0, 20));
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

        if (request.getCategoria() != null && request.getCategoria().getIdCategoriaPadre() != null) {
            Categoria categoria = categoriaRepository.findById(request.getCategoria().getIdCategoriaPadre())
                    .orElseThrow(() -> new EntityNotFoundException("No se encontró la categoría con ID: " + request.getCategoria().getIdCategoriaPadre()));
            existing.setCategoria(categoria);
        }

        ArticuloManufacturado updated = manufacturadoMapper.toEntity(request);
        updated.setId(existing.getId());

        if (request.getPrecioVenta() != null) {
            updated.calcularPrecioVenta(request.getPrecioVenta());
        }

        ArticuloManufacturado saved = manufacturadoRepository.save(updated);
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