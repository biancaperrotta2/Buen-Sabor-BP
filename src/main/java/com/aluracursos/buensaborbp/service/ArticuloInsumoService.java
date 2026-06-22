package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.ArticuloInsumoBaseResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoFullResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoRequest;
import com.aluracursos.buensaborbp.entity.ArticuloInsumo;
import java.util.List;

public interface ArticuloInsumoService extends BaseService<ArticuloInsumo, Long> {
    List<ArticuloInsumoBaseResponse> getAll();
    ArticuloInsumoFullResponse getById(Long id);
    ArticuloInsumoFullResponse crearArticuloInsumo(ArticuloInsumoRequest request);
    ArticuloInsumoFullResponse update(Long id, ArticuloInsumoRequest request);
    void delete(Long id);
    List<ArticuloInsumoBaseResponse> getActivos();
    
    // MÉTODOS DE LÓGICA DE NEGOCIO QUE AGREGAMOS:
    void reservarStock(Long id, double cantidad, String articuloNombre);
    void liberarStock(Long id, double cantidad);
    void confirmarVenta(Long id, double cantidad);
}

