package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.entity.Empleado;
import com.aluracursos.buensaborbp.dto.EmpleadoBaseResponse;
import com.aluracursos.buensaborbp.dto.EmpleadoFullResponse;
import com.aluracursos.buensaborbp.dto.EmpleadoRequest;
import java.util.List;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import com.aluracursos.buensaborbp.repository.EmpleadoRepository;
import com.aluracursos.buensaborbp.mapper.EmpleadoMapper;
import org.springframework.security.crypto.password.PasswordEncoder;


public class EmpleadoServiceImpl extends BaseServiceImpl<Empleado, Long> implements EmpleadoService{
    
    private final EmpleadoRepository empleadoRepository;
    private final EmpleadoMapper empleadoMapper;
    private final PasswordEncoder passwordEncoder; 

    public EmpleadoServiceImpl(EmpleadoRepository empleadoRepository, EmpleadoMapper empleadoMapper, PasswordEncoder passwordEncoder) {
        super(empleadoRepository);
        this.empleadoRepository = empleadoRepository;
        this.empleadoMapper = empleadoMapper;
        this.passwordEncoder = passwordEncoder; 
    }

    @Override
    public EmpleadoBaseResponse buscarEmpleadoID(Long id) {
        Empleado empleado = empleadoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado"));
        return empleadoMapper.toBaseResponse(empleado);
    }

    @Override
    public List<EmpleadoBaseResponse> mostrarEmpleados() {
        List<Empleado> empleados = empleadoRepository.findAll();
        return empleadoMapper.toBaseResponseList(empleados);
    }
    
    @Override
    @Transactional
    public EmpleadoFullResponse crearEmpleado(EmpleadoRequest request) {
        Empleado empleado = empleadoMapper.toEntity(request);
        
        empleado.setRol(request.rol()); 
        
        empleado.setPassword(passwordEncoder.encode(request.password()));
        return empleadoMapper.toFullResponse(empleadoRepository.save(empleado));
    }
    
    
    @Override
    @Transactional
    public EmpleadoFullResponse actualizarEmpleado(Long id, EmpleadoRequest request) {
        Empleado empleado = empleadoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado"));
        empleadoMapper.updateEntity(request, empleado);
        empleado = empleadoRepository.save(empleado);
        return empleadoMapper.toFullResponse(empleado);
    }
    
    
    @Override
    @Transactional
    public void desactivarEmpleado(Long id) {
        Empleado empleado = empleadoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado"));
        empleado.setActivo(false);
        empleadoRepository.save(empleado);
    }
}
