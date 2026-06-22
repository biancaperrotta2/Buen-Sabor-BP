package com.aluracursos.buensaborbp.service;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoBaseResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoFullResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoRequest;
import com.aluracursos.buensaborbp.entity.ArticuloInsumo;
import com.aluracursos.buensaborbp.entity.Categoria;
import com.aluracursos.buensaborbp.mapper.ArticuloInsumoMapper;
import com.aluracursos.buensaborbp.repository.ArticuloInsumoRepository;
import com.aluracursos.buensaborbp.repository.CategoriaRepository;

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
    public List<ArticuloInsumoBaseResponse> getAll() {
        return articuloInsumoRepository.findAll()
                .stream()
                .map(articuloInsumoMapper::toBaseResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ArticuloInsumoFullResponse getById(Long id) {
        ArticuloInsumo entity = articuloInsumoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el insumo con ID: " + id));
        return articuloInsumoMapper.toFullResponse(entity);
    }

    @Override
    public ArticuloInsumoFullResponse crearArticuloInsumo(ArticuloInsumoRequest request) {
        if (request.getCategoria() == null || request.getCategoria().getIdCategoriaPadre() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        
        Categoria categoria = categoriaRepository.findById(request.getCategoria().getIdCategoriaPadre())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        
        ArticuloInsumo insumo = articuloInsumoMapper.toEntity(request);
        insumo.setCategoria(categoria);
        
        if (request.getPrecioVenta() != null) {
            insumo.calcularPrecioVenta(request.getPrecioVenta());
        }
        
        return articuloInsumoMapper.toFullResponse(articuloInsumoRepository.save(insumo));
    }

    @Override
    public ArticuloInsumoFullResponse update(Long id, ArticuloInsumoRequest request) {
        ArticuloInsumo existing = articuloInsumoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el insumo con ID: " + id));
        
        if (request.getCategoria() != null && request.getCategoria().getIdCategoriaPadre() != null) {
            Categoria categoria = categoriaRepository.findById(request.getCategoria().getIdCategoriaPadre())
                    .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
            existing.setCategoria(categoria);
        }
        
        // Actualizamos los campos manualmente para no perder el stockActual que está en la DB
        existing.setNombre(request.getNombre());
        existing.setPrecioCosto(request.getPrecioCosto());
        existing.setMargen(request.getMargen());
        existing.setStockMaximo(request.getStockMaximo());
        existing.setStockMinimo(request.getStockMinimo());
        
        if (request.getPrecioVenta() != null) {
            existing.calcularPrecioVenta(request.getPrecioVenta());
        }
        
        return articuloInsumoMapper.toFullResponse(articuloInsumoRepository.save(existing));
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