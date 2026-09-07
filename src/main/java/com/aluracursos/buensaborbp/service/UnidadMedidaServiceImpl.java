package com.aluracursos.buensaborbp.service;

import org.springframework.stereotype.Service;
import com.aluracursos.buensaborbp.entity.UnidadMedida;
import com.aluracursos.buensaborbp.repository.UnidadMedidaRepository;

@Service
public class UnidadMedidaServiceImpl extends BaseServiceImpl<UnidadMedida, Long> implements UnidadMedidaService {

    private final UnidadMedidaRepository unidadMedidaRepository;

    public UnidadMedidaServiceImpl(UnidadMedidaRepository unidadMedidaRepository) {
        super(unidadMedidaRepository);
        this.unidadMedidaRepository = unidadMedidaRepository;
    }
}