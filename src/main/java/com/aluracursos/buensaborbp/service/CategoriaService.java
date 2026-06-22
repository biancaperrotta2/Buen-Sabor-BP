package com.aluracursos.buensaborbp.service;

import java.util.List;
import com.aluracursos.buensaborbp.dto.CategoriaBaseResponse;
import com.aluracursos.buensaborbp.dto.CategoriaFullResponse;
import com.aluracursos.buensaborbp.dto.CategoriaRequest;
import com.aluracursos.buensaborbp.entity.Categoria;

public interface CategoriaService extends BaseService<Categoria, Long>{
    CategoriaFullResponse crearCategoria(CategoriaRequest request);
    CategoriaFullResponse actualizarCategoria(Long id, CategoriaRequest request);
    CategoriaBaseResponse buscarCategoriaID(Long id);
    List<CategoriaBaseResponse> mostrarCategorias();
    void desactivarCategoria(Long id);
}
