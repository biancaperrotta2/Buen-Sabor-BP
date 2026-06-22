package com.aluracursos.buensaborbp.service;

import java.util.List;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.aluracursos.buensaborbp.dto.CategoriaBaseResponse;
import com.aluracursos.buensaborbp.dto.CategoriaFullResponse;
import com.aluracursos.buensaborbp.entity.Categoria;
import com.aluracursos.buensaborbp.dto.CategoriaRequest;
import com.aluracursos.buensaborbp.mapper.CategoriaMapper;
import com.aluracursos.buensaborbp.repository.CategoriaRepository;

@Service
public class CategoriaServiceImpl extends BaseServiceImpl<Categoria, Long> implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository, CategoriaMapper categoriaMapper) {
        super(categoriaRepository);
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
    }

    @Override
    @Transactional
    public List<CategoriaBaseResponse> mostrarCategorias() {
        List<Categoria> principales = categoriaRepository.findByCategoriaPadreIsNullAndActivoTrue();
        return categoriaMapper.toBaseResponseList(principales);
    }

    @Override
    @Transactional
    public CategoriaBaseResponse buscarCategoriaID(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + id));
        return categoriaMapper.toBaseResponse(categoria);
    }

    @Override
    @Transactional
    public CategoriaFullResponse crearCategoria(CategoriaRequest request) {
        Categoria nueva = categoriaMapper.toEntity(request);

        if (request.idCategoriaPadre() != null) {
            Categoria padre = categoriaRepository.findById(request.idCategoriaPadre())
                .orElseThrow(() -> new EntityNotFoundException("La categoría padre no existe"));
            nueva.setCategoriaPadre(padre);
        }

        return categoriaMapper.toFullResponse(categoriaRepository.save(nueva));
    }

    @Override
    @Transactional
    public CategoriaFullResponse actualizarCategoria(Long id, CategoriaRequest request) {
        Categoria existente = categoriaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        existente.setDenominacion(request.denominacion());

        if (request.idCategoriaPadre() != null) {
            Categoria padre = categoriaRepository.findById(request.idCategoriaPadre()).orElseThrow();
            existente.setCategoriaPadre(padre);
        } else {
            existente.setCategoriaPadre(null);
        }

        return categoriaMapper.toFullResponse(categoriaRepository.save(existente));
    }

    @Override
    @Transactional
    public void desactivarCategoria(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
        
        desactivarRecursivo(categoria);
    }

    private void desactivarRecursivo(Categoria categoria) {
        categoria.setActivo(false);
        if (categoria.getSubcategorias() != null) {
            categoria.getSubcategorias().forEach(this::desactivarRecursivo);
        }
        categoriaRepository.save(categoria);
    }
}
