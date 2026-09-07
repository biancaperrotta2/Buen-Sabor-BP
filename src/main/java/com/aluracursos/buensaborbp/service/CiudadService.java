package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.CiudadResponse;
import java.math.BigDecimal;
import java.util.List;
import com.aluracursos.buensaborbp.dto.CiudadRequest;
import com.aluracursos.buensaborbp.entity.Ciudad;

public interface CiudadService extends BaseService<Ciudad, Long> {
    List<CiudadResponse> getAll();
    List<CiudadResponse> listarPorProvincia(Long provinciaId);
    CiudadResponse crearCiudad(CiudadRequest request);
    CiudadResponse actualizarCostoEnvio(Long id, BigDecimal costoEnvio);
}