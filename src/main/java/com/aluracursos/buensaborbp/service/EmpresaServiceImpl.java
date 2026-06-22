package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.entity.Empresa;
import com.aluracursos.buensaborbp.dto.EmpresaRequest;
import com.aluracursos.buensaborbp.dto.EmpresaResponse;
import com.aluracursos.buensaborbp.mapper.EmpresaMapper;
import com.aluracursos.buensaborbp.repository.EmpresaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class EmpresaServiceImpl extends BaseServiceImpl<Empresa, Long> implements EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final EmpresaMapper empresaMapper;

    public EmpresaServiceImpl(EmpresaRepository empresaRepository, EmpresaMapper empresaMapper) {
        super(empresaRepository);
        this.empresaRepository = empresaRepository;
        this.empresaMapper = empresaMapper;
    }

    @Override
    public EmpresaResponse obtenerEmpresaUnica() {
        Empresa empresa = empresaRepository.findAll().stream()
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("No se encontró la configuración de la empresa."));
        
        return empresaMapper.toResponse(empresa);
    }

    @Override
    @Transactional
    public EmpresaResponse actualizarDatosEmpresa(Long id, EmpresaRequest request) {
        Empresa existente = empresaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada con ID: " + id));

        empresaMapper.updateEntity(request, existente);
        
        return empresaMapper.toResponse(empresaRepository.save(existente));
    }
}