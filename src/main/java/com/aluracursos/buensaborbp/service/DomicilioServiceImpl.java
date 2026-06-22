package com.aluracursos.buensaborbp.service;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import com.aluracursos.buensaborbp.dto.DomicilioBaseResponse;
import com.aluracursos.buensaborbp.dto.DomicilioFullResponse;
import com.aluracursos.buensaborbp.dto.DomicilioRequest;
import com.aluracursos.buensaborbp.entity.Ciudad;
import com.aluracursos.buensaborbp.entity.Domicilio;
import com.aluracursos.buensaborbp.mapper.DomicilioMapper;
import com.aluracursos.buensaborbp.repository.CiudadRepository;
import com.aluracursos.buensaborbp.repository.DomicilioRepository;

@Service
public class DomicilioServiceImpl extends BaseServiceImpl<Domicilio, Long> implements DomicilioService{
    private final DomicilioMapper domicilioMapper;
    private final DomicilioRepository domicilioRepository;
    private final CiudadRepository ciudadRepository;

    public DomicilioServiceImpl(DomicilioMapper domicilioMapper, DomicilioRepository domicilioRepository, CiudadRepository ciudadRepository){
        super(domicilioRepository);
        this.domicilioMapper = domicilioMapper;
        this.domicilioRepository = domicilioRepository;
        this.ciudadRepository = ciudadRepository;
    }

    //IMPLEMENTACION DE METODOS:
    //metodo para crear un nuevo domicilio
    @Override
    @Transactional
    public DomicilioFullResponse crearDomicilio(DomicilioRequest request) {
        Ciudad ciudad = ciudadRepository.findById(request.idCiudad()) 
            .orElseThrow(() -> new EntityNotFoundException("La ciudad seleccionada no existe."));

        Domicilio domicilio = domicilioMapper.toEntity(request);
        
        domicilio.setCiudad(ciudad);
        domicilio.setActivo(true); 

        return domicilioMapper.toFullResponse(domicilioRepository.save(domicilio));
    }

    //actualizar domicilio
    @Override
    @Transactional
    public DomicilioFullResponse actualizarDomicilio(Long id, DomicilioRequest request) {
        Domicilio existente = domicilioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Domicilio no encontrado"));

        domicilioMapper.updateEntityFromDto(request, existente);

        Ciudad nuevaCiudad = ciudadRepository.findById(request.idCiudad())
            .orElseThrow(() -> new EntityNotFoundException("La ciudad seleccionada no existe"));
        
        existente.setCiudad(nuevaCiudad);

        return domicilioMapper.toFullResponse(domicilioRepository.save(existente));
    }

    //mostrar todos los domicilios
    @Override
    public List<DomicilioBaseResponse> mostrarDomicilios(){
        return domicilioRepository.findAll()
            .stream()
            .map(domicilioMapper :: toBaseResponse)
            .collect(Collectors.toList());
    }

    //buscar domicilio por id?
    @Override
    public DomicilioBaseResponse buscarDomicilioID(Long id){
        Domicilio entity = domicilioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("No se encontro un domicilio con ID: " + id));
            return domicilioMapper.toBaseResponse(entity);
    }

    //eliminar domicilio
    @Override 
    public void desactivarDomicilio(Long id){
        Domicilio domicilio = domicilioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("No se encontró un domicilio con ID: " + id));
        domicilio.setActivo(false);
        domicilioRepository.save(domicilio);
    }
    }