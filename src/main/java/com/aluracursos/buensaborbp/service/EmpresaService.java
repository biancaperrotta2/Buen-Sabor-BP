package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.EmpresaRequest;
import com.aluracursos.buensaborbp.dto.EmpresaResponse;


public interface EmpresaService {
    EmpresaResponse obtenerEmpresaUnica(); 

    EmpresaResponse actualizarDatosEmpresa(Long id, EmpresaRequest request);
}