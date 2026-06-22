package com.aluracursos.buensaborbp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;
import java.util.List;
import com.aluracursos.buensaborbp.dto.SucursalRequest;
import com.aluracursos.buensaborbp.dto.SucursalResponse;
import com.aluracursos.buensaborbp.entity.Sucursal;
import com.aluracursos.buensaborbp.mapper.SucursalMapper;
import com.aluracursos.buensaborbp.repository.SucursalRepository;
import jakarta.persistence.EntityNotFoundException;
import com.aluracursos.buensaborbp.entity.Empresa;
import com.aluracursos.buensaborbp.entity.Ciudad;
import com.aluracursos.buensaborbp.entity.Domicilio;
import com.aluracursos.buensaborbp.repository.EmpresaRepository;
import com.aluracursos.buensaborbp.repository.CiudadRepository;

@Service
public class SucursalServiceImpl extends BaseServiceImpl<Sucursal, Long> implements SucursalService {

    private final SucursalRepository sucursalRepository;
    private final SucursalMapper sucursalMapper;
    private final EmpresaRepository empresaRepository; 
    private final CiudadRepository ciudadRepository;   

    public SucursalServiceImpl(SucursalRepository sucursalRepository, 
                               SucursalMapper sucursalMapper,
                               EmpresaRepository empresaRepository,
                               CiudadRepository ciudadRepository) {
        super(sucursalRepository);
        this.sucursalRepository = sucursalRepository;
        this.sucursalMapper = sucursalMapper;
        this.empresaRepository = empresaRepository;
        this.ciudadRepository = ciudadRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SucursalResponse> listarActivas() {
        return sucursalMapper.toResponseList(sucursalRepository.findByActivoTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaAbierta(Long id) {
        Sucursal s = sucursalRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));
        
        LocalTime ahora = LocalTime.now();
        // Lógica: Está abierta si la hora actual está entre apertura y cierre
        return ahora.isAfter(s.getHorarioApertura()) && ahora.isBefore(s.getHorarioCierre());
    }

    @Override
    @Transactional
    public SucursalResponse crearSucursal(SucursalRequest request) {
        Empresa empresa = empresaRepository.findById(request.idEmpresa())
            .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));

        Ciudad ciudad = ciudadRepository.findById(request.domicilio().idCiudad())
            .orElseThrow(() -> new EntityNotFoundException("Localidad no encontrada"));

        // Construcción del Domicilio de la sucursal
        Domicilio domicilio = Domicilio.builder()
            .calle(request.domicilio().calle())
            .numero(request.domicilio().numero())
            .cp(request.domicilio().cp())
            .piso(request.domicilio().piso())
            .nroDepto(request.domicilio().nroDepto())
            .ciudad(ciudad)
            .build();

        Sucursal sucursal = Sucursal.builder()
            .nombre(request.nombre())
            .horarioApertura(request.horarioApertura())
            .horarioCierre(request.horarioCierre())
            .empresa(empresa)
            .domicilio(domicilio)
            .activo(true)
            .build();

        return sucursalMapper.toResponse(sucursalRepository.save(sucursal));
    }

    @Override
    @Transactional
    public SucursalResponse actualizarSucursal(Long id, SucursalRequest request) {
        Sucursal sucursal = sucursalRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));

        // Actualizamos los campos básicos
        sucursal.setNombre(request.nombre());
        sucursal.setHorarioApertura(request.horarioApertura());
        sucursal.setHorarioCierre(request.horarioCierre());

        // Actualizamos el domicilio si es necesario
        Domicilio dom = sucursal.getDomicilio();
        dom.setCalle(request.domicilio().calle());
        dom.setNumero(request.domicilio().numero());
        dom.setCp(request.domicilio().cp());
        
        if (dom.getCiudad().getId() != request.domicilio().idCiudad()) {
            Ciudad nuevaCiudad = ciudadRepository.findById(request.domicilio().idCiudad())
                .orElseThrow(() -> new EntityNotFoundException("Nueva localidad no encontrada"));
            dom.setCiudad(nuevaCiudad);
        }

        return sucursalMapper.toResponse(sucursalRepository.save(sucursal));
    }

    @Override
    @Transactional
    public void eliminarLogico(Long id) {
        Sucursal sucursal = sucursalRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));
        
        //solo la desactivamos
        sucursal.setActivo(false);
        sucursalRepository.save(sucursal);
    }
}