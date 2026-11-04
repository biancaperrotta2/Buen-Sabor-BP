package com.aluracursos.buensaborbp.service;

import java.util.List;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.aluracursos.buensaborbp.dto.CategoriaBaseResponse;
import com.aluracursos.buensaborbp.dto.CategoriaFullResponse;
import com.aluracursos.buensaborbp.dto.PageResponse;
import com.aluracursos.buensaborbp.entity.Categoria;
import com.aluracursos.buensaborbp.entity.Enums.TipoCategoria;
import com.aluracursos.buensaborbp.dto.CategoriaRequest;
import com.aluracursos.buensaborbp.mapper.CategoriaMapper;
import com.aluracursos.buensaborbp.repository.ArticuloRepository;
import com.aluracursos.buensaborbp.repository.CategoriaRepository;
import java.util.ArrayList;
import org.springframework.data.domain.Pageable;

@Service
public class CategoriaServiceImpl extends BaseServiceImpl<Categoria, Long> implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;
    private final ArticuloRepository articuloRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository, CategoriaMapper categoriaMapper,
                                ArticuloRepository articuloRepository) {
        super(categoriaRepository);
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
        this.articuloRepository = articuloRepository;
    }

    @Override
    @Transactional
    public List<CategoriaBaseResponse> mostrarCategorias() {
        List<Categoria> principales = categoriaRepository.findByCategoriaPadreIsNullAndActivoTrue();
        return categoriaMapper.toBaseResponseList(principales);
    }

    @Override
    @Transactional
    public PageResponse<CategoriaBaseResponse> mostrarCategoriasAdmin(String busqueda, Pageable pageable) {
        String filtro = busqueda != null ? busqueda.trim() : "";
        return PageResponse.de(categoriaRepository.buscarPrincipales(filtro, pageable), categoriaMapper::toBaseResponse);
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
    public List<CategoriaBaseResponse> listarHijas(Long categoriaPadreId) {
        return categoriaMapper.toBaseResponseList(categoriaRepository.findByCategoriaPadreIdAndActivoTrue(categoriaPadreId));
    }

    @Override
    @Transactional
    public CategoriaFullResponse crearCategoria(CategoriaRequest request) {
        Categoria nueva = categoriaMapper.toEntity(request);
        nueva.setTipo(tipoPedido(request));

        if (request.idCategoriaPadre() != null) {
            Categoria padre = categoriaRepository.findById(request.idCategoriaPadre())
                .orElseThrow(() -> new EntityNotFoundException("La categoría padre no existe"));
            nueva.setCategoriaPadre(padre);
            nueva.setTipo(padre.getTipo());
        }

        return categoriaMapper.toFullResponse(categoriaRepository.save(nueva));
    }

    private TipoCategoria tipoPedido(CategoriaRequest request) {
        return request.tipo() != null ? request.tipo() : TipoCategoria.PRODUCTO;
    }

    // Cambiar el tipo con artículos adentro los dejaría en una categoría que no les corresponde
    // (ej. platos en una categoría de Insumos), así que se exige moverlos antes. Cuenta también
    // los de las subcategorías, porque el tipo nuevo les llega a ellas.
    private void exigirSinArticulos(Categoria categoria) {
        List<Long> ids = new ArrayList<>();
        juntarIds(categoria, ids);
        long cantidad = articuloRepository.countByCategoria_IdIn(ids);
        if (cantidad > 0) {
            throw new IllegalStateException("No se puede cambiar el tipo de \"" + categoria.getDenominacion()
                    + "\": tiene " + cantidad + (cantidad == 1 ? " artículo cargado" : " artículos cargados")
                    + " (productos, insumos o promociones, contando sus subcategorías). Movelos a otra categoría primero.");
        }
    }

    private void juntarIds(Categoria categoria, List<Long> ids) {
        ids.add(categoria.getId());
        if (categoria.getSubcategorias() != null) {
            categoria.getSubcategorias().forEach(hija -> juntarIds(hija, ids));
        }
    }

    // Una subcategoría nunca puede ser de otro tipo que su padre.
    private void aplicarTipoRecursivo(Categoria categoria, TipoCategoria tipo) {
        categoria.setTipo(tipo);
        if (categoria.getSubcategorias() != null) {
            categoria.getSubcategorias().forEach(hija -> aplicarTipoRecursivo(hija, tipo));
        }
    }

    @Override
    @Transactional
    public CategoriaFullResponse actualizarCategoria(Long id, CategoriaRequest request) {
        Categoria existente = categoriaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        existente.setDenominacion(request.denominacion());

        Categoria padre = request.idCategoriaPadre() != null
                ? categoriaRepository.findById(request.idCategoriaPadre()).orElseThrow()
                : null;
        TipoCategoria tipoNuevo = padre != null ? padre.getTipo()
                : (request.tipo() != null ? request.tipo() : existente.getTipo());
        if (tipoNuevo != existente.getTipo()) {
            exigirSinArticulos(existente);
        }

        existente.setCategoriaPadre(padre);
        aplicarTipoRecursivo(existente, tipoNuevo);

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
