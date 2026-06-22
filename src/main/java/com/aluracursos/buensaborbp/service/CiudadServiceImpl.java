package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.CiudadResponse;
import com.aluracursos.buensaborbp.entity.Ciudad;
import com.aluracursos.buensaborbp.mapper.CiudadMapper;
import com.aluracursos.buensaborbp.repository.CiudadRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import com.aluracursos.buensaborbp.dto.CiudadRequest;
import com.aluracursos.buensaborbp.entity.Provincia;
import com.aluracursos.buensaborbp.repository.ProvinciaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CiudadServiceImpl extends BaseServiceImpl<Ciudad, Long> implements CiudadService {

    private final CiudadRepository ciudadRepository;
    private final ProvinciaRepository provinciaRepository;
    private final CiudadMapper ciudadMapper;

    public CiudadServiceImpl(CiudadRepository ciudadRepository, 
                             ProvinciaRepository provinciaRepository, 
                             CiudadMapper ciudadMapper) {
        super(ciudadRepository);
        this.ciudadRepository = ciudadRepository;
        this.provinciaRepository = provinciaRepository;
        this.ciudadMapper = ciudadMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CiudadResponse> listarPorProvincia(Long provinciaId) {
        return ciudadMapper.toResponseList(ciudadRepository.findByProvinciaId(provinciaId));
    }

    @Override
    @Transactional
    public CiudadResponse crearCiudad(CiudadRequest request) {
        Provincia provincia = provinciaRepository.findById(request.idProvincia())
                .orElseThrow(() -> new EntityNotFoundException("Provincia no encontrada"));

        Ciudad ciudad = Ciudad.builder()
                .nombre(request.nombre())
                .provincia(provincia)
                .build();

        return ciudadMapper.toResponse(ciudadRepository.save(ciudad));
    }
}