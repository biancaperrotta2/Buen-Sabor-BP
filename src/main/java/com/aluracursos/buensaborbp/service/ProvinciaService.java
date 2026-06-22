package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.ProvinciaRequest;
import com.aluracursos.buensaborbp.dto.ProvinciaResponse;
import com.aluracursos.buensaborbp.entity.Provincia;
import java.util.List;

public interface ProvinciaService extends BaseService<Provincia, Long> {
    List<ProvinciaResponse> listarTodas();
    ProvinciaResponse crearProvincia(ProvinciaRequest request);
}
