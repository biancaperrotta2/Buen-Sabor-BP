package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.ArticuloInsumoBaseResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoFullResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoRequest;
import com.aluracursos.buensaborbp.entity.ArticuloInsumo;
import com.aluracursos.buensaborbp.entity.Categoria;
import com.aluracursos.buensaborbp.entity.Sucursal;
import com.aluracursos.buensaborbp.mapper.ArticuloInsumoMapper;
import com.aluracursos.buensaborbp.repository.ArticuloInsumoRepository;
import com.aluracursos.buensaborbp.repository.ArticuloManufacturadoDetalleRepository;
import com.aluracursos.buensaborbp.repository.CategoriaRepository;
import com.aluracursos.buensaborbp.repository.PromocionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticuloInsumoServiceImpl implements ArticuloInsumoService{
    private final ArticuloInsumoRepository ArticuloInsumoRepository;
    private final ArticuloInsumoMapper ArticuloInsumoMapper;
    private final CategoriaRepository categoriaRepository;
    private final ArticuloManufacturadoDetalleRepository articuloManufacturadoDetalleRepository;
    private final PromocionRepository promocionRepository;
    private final ArticuloInsumoMapper articuloInsumoMapper;

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
    public ArticuloInsumoFullResponse crearArticuloInsumo(ArticuloInsumoRequest request) {
        Categoria categoria = categoriaRepository.findById(request.getCategoria().getIdCategoriaPadre()).orElseThrow(()-> new EntityNotFoundException("No se encontro la categoría"));
        ArticuloInsumo insumo = articuloInsumoMapper.toEntity(request);
        Sucursal sucursal = Sucursal.builder().id_sucursal(1L).build();
        insumo.setCategoria(categoria);
        insumo.getPrecioVenta(request.setPrecioVenta(BigDecimal precioVenta));
        return null;
    }

    @Override
    public ArticuloInsumoFullResponse update(Long id, ArticuloInsumoRequest request) {
        ArticuloInsumo existing = findById(id);
        ArticuloInsumo entity = mapper.toEntity(request);
        entity.setId(existing.getId());
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
