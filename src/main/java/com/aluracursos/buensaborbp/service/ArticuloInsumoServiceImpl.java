package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.ArticuloInsumoBaseResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoFullResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoRequest;
import com.aluracursos.buensaborbp.entity.ArticuloInsumo;
import com.aluracursos.buensaborbp.entity.Categoria;
import com.aluracursos.buensaborbp.mapper.ArticuloInsumoMapper;
import com.aluracursos.buensaborbp.repository.ArticuloInsumoRepository;
import com.aluracursos.buensaborbp.repository.CategoriaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de artículos insumo.
 * Proporciona operaciones CRUD y lógica de negocio relacionada con insumos.
 */
@Service
@Transactional
public class ArticuloInsumoServiceImpl extends BaseServiceImpl<ArticuloInsumo, Long> implements ArticuloInsumoService {
    
    private final ArticuloInsumoRepository articuloInsumoRepository;
    private final ArticuloInsumoMapper articuloInsumoMapper;
    private final CategoriaRepository categoriaRepository;

    public ArticuloInsumoServiceImpl(
            ArticuloInsumoRepository articuloInsumoRepository,
            ArticuloInsumoMapper articuloInsumoMapper,
            CategoriaRepository categoriaRepository) {
        super(articuloInsumoRepository);
        this.articuloInsumoRepository = articuloInsumoRepository;
        this.articuloInsumoMapper = articuloInsumoMapper;
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * Obtiene todos los artículos insumo.
     * 
     * @return Lista de respuestas base de artículos insumo
     */
    @Override
    public List<ArticuloInsumoBaseResponse> getAll() {
        return articuloInsumoRepository.findAll()
                .stream()
                .map(articuloInsumoMapper::toBaseResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca y devuelve un insumo por su ID.
     * 
     * @param id ID del insumo a buscar
     * @return Respuesta completa del insumo
     * @throws EntityNotFoundException si no se encuentra el insumo
     */
    @Override
    public ArticuloInsumoFullResponse getById(Long id) {
        ArticuloInsumo entity = articuloInsumoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el insumo con ID: " + id));
        return articuloInsumoMapper.toFullResponse(entity);
    }

    /**
     * Crea un nuevo artículo insumo.
     * 
     * @param request Datos del insumo a crear
     * @return Respuesta completa del insumo creado
     * @throws EntityNotFoundException si no se encuentra la categoría
     */
    @Override
    public ArticuloInsumoFullResponse crearArticuloInsumo(ArticuloInsumoRequest request) {
        // Validar que la categoría existe
        if (request.getCategoria() == null || request.getCategoria().getIdCategoriaPadre() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        
        Categoria categoria = categoriaRepository.findById(request.getCategoria().getIdCategoriaPadre())
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la categoría con ID: " + request.getCategoria().getIdCategoriaPadre()));
        
        // Mapear el request a entidad
        ArticuloInsumo insumo = articuloInsumoMapper.toEntity(request);
        insumo.setCategoria(categoria);
        
        // Calcular precio de venta si se proporciona margen
        if (request.getPrecioVenta() != null) {
            insumo.calcularPrecioVenta(request.getPrecioVenta());
        }
        
        // Guardar el insumo
        ArticuloInsumo saved = articuloInsumoRepository.save(insumo);
        
        return articuloInsumoMapper.toFullResponse(saved);
    }

    /**
     * Actualiza un artículo insumo existente.
     * 
     * @param id ID del insumo a actualizar
     * @param request Datos actualizados del insumo
     * @return Respuesta completa del insumo actualizado
     * @throws EntityNotFoundException si no se encuentra el insumo o la categoría
     */
    @Override
    public ArticuloInsumoFullResponse update(Long id, ArticuloInsumoRequest request) {
        ArticuloInsumo existing = articuloInsumoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el insumo con ID: " + id));
        
        // Actualizar categoría si se proporciona
        if (request.getCategoria() != null && request.getCategoria().getIdCategoriaPadre() != null) {
            Categoria categoria = categoriaRepository.findById(request.getCategoria().getIdCategoriaPadre())
                    .orElseThrow(() -> new EntityNotFoundException("No se encontró la categoría con ID: " + request.getCategoria().getIdCategoriaPadre()));
            existing.setCategoria(categoria);
        }
        
        // Mapear campos del request
        ArticuloInsumo updated = articuloInsumoMapper.toEntity(request);
        updated.setId(existing.getId());
        
        // Calcular precio de venta si se proporciona
        if (request.getPrecioVenta() != null) {
            updated.calcularPrecioVenta(request.getPrecioVenta());
        }
        
        ArticuloInsumo saved = articuloInsumoRepository.save(updated);
        return articuloInsumoMapper.toFullResponse(saved);
    }

    /**
     * Elimina un artículo insumo.
     * 
     * @param id ID del insumo a eliminar
     * @throws EntityNotFoundException si no se encuentra el insumo
     */
    @Override
    public void delete(Long id) {
        if (!articuloInsumoRepository.existsById(id)) {
            throw new EntityNotFoundException("No se encontró el insumo con ID: " + id);
        }
        articuloInsumoRepository.deleteById(id);
    }

    /**
     * Obtiene todos los insumos activos (disponibles).
     * 
     * @return Lista de respuestas base de insumos activos
     */
    @Override
    public List<ArticuloInsumoBaseResponse> getActivos() {
        return articuloInsumoRepository.findByActivoTrue()
                .stream()
                .map(articuloInsumoMapper::toBaseResponse)
                .collect(Collectors.toList());
    }
}
