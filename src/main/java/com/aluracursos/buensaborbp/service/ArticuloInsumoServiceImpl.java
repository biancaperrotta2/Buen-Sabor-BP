package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.ArticuloInsumoBaseResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoFullResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoRequest;
import com.aluracursos.buensaborbp.entity.ArticuloInsumo;
import com.aluracursos.buensaborbp.mapper.ArticuloInsumoMapper;
import com.aluracursos.buensaborbp.repository.ArticuloInsumoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticuloInsumoServiceImpl extends BaseServiceImpl<ArticuloInsumo, Long> implements ArticuloInsumoService{
    private final ArticuloInsumoRepository repository;
    private final ArticuloInsumoMapper mapper;

    public ArticuloInsumoServiceImpl(ArticuloInsumoRepository repository, ArticuloInsumoMapper mapper) {
        super(repository);
        this.repository = repository;
        this.mapper = mapper;
    }

    //devuelve todos los articulos insumos
    @Override
    public List<ArticuloInsumoBaseResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toBaseResponse)
                .collect(Collectors.toList());
    }

    //busca y devuelve un insumo por su ID
    @Override
    public ArticuloInsumoFullResponse getById(Long id) {
        ArticuloInsumo entity = findById(id);
        return mapper.toFullResponse(entity);
    }

    //crea un articulo insumo
    @Override
    public ArticuloInsumoFullResponse create(ArticuloInsumoRequest request) {
        ArticuloInsumo entity = mapper.toEntity(request);
        ArticuloInsumo saved = super.create(entity);
        return mapper.toFullResponse(saved);
    }

    @Override
    public ArticuloInsumoFullResponse update(Long id, ArticuloInsumoRequest request) {
        ArticuloInsumo existing = findById(id);
        ArticuloInsumo entity = mapper.toEntity(request);
        entity.setId_articulo(existing.getId_articulo());
        ArticuloInsumo updated = super.update(id, entity);
        return mapper.toFullResponse(updated);
    }

    //elimina un articulo insumo
    @Override
    public void delete(Long id) {
        super.delete(id);
    }

    //nos devuelve todos los insumos activos (disponibles)
    @Override
    public List<ArticuloInsumoBaseResponse> getActivos() {
        return repository.findByActivoTrue()
                .stream()
                .map(mapper::toBaseResponse)
                .collect(Collectors.toList());
    }
}
