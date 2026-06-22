package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.CiudadResponse;
import java.util.List;
import com.aluracursos.buensaborbp.dto.CiudadRequest;
import com.aluracursos.buensaborbp.entity.Ciudad;

public interface CiudadService extends BaseService<Ciudad, Long> {
    List<CiudadResponse> listarPorProvincia(Long provinciaId);
    CiudadResponse crearCiudad(CiudadRequest request);
}