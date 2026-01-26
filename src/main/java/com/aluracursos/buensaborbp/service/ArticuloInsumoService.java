package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.ArticuloInsumoBaseResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoFullResponse;
import com.aluracursos.buensaborbp.dto.ArticuloInsumoRequest;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public interface ArticuloInsumoService {
    List<ArticuloInsumoBaseResponse> getAll();
    ArticuloInsumoFullResponse getById(Long id);
    ArticuloInsumoFullResponse crearArticuloInsumo(ArticuloInsumoRequest request);
    ArticuloInsumoFullResponse update(Long id, ArticuloInsumoRequest request);
    void delete(Long id);

    // Ejemplo de metodo adicional opcional
    List<ArticuloInsumoBaseResponse> getActivos();
}

