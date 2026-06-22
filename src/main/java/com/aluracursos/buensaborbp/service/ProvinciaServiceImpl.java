package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.ProvinciaRequest;
import com.aluracursos.buensaborbp.dto.ProvinciaResponse;
import com.aluracursos.buensaborbp.entity.Provincia;
import com.aluracursos.buensaborbp.repository.ProvinciaRepository;
import com.aluracursos.buensaborbp.mapper.ProvinciaMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProvinciaServiceImpl extends BaseServiceImpl<Provincia, Long> implements ProvinciaService {

    private final ProvinciaRepository provinciaRepository;
    private final ProvinciaMapper provinciaMapper;

    public ProvinciaServiceImpl(ProvinciaRepository provinciaRepository, ProvinciaMapper provinciaMapper) {
        super(provinciaRepository);
        this.provinciaRepository = provinciaRepository;
        this.provinciaMapper = provinciaMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProvinciaResponse> listarTodas() {
        return provinciaMapper.toResponseList(provinciaRepository.findAll());
    }

    @Override
    @Transactional
    public ProvinciaResponse crearProvincia(ProvinciaRequest request) {
        Provincia provincia = Provincia.builder()
                .nombre(request.nombre())
                .build();
        
        Provincia guardada = provinciaRepository.save(provincia);
        return provinciaMapper.toResponse(guardada);
    }
}