package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.entity.Empleado;
import com.aluracursos.buensaborbp.dto.EmpleadoBaseResponse;
import com.aluracursos.buensaborbp.dto.EmpleadoFullResponse;
import com.aluracursos.buensaborbp.dto.EmpleadoRequest;
import com.aluracursos.buensaborbp.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface EmpleadoService extends BaseService<Empleado, Long>{
    EmpleadoBaseResponse buscarEmpleadoID(Long id);
    PageResponse<EmpleadoBaseResponse> mostrarEmpleados(String busqueda, Pageable pageable);
    EmpleadoFullResponse crearEmpleado(EmpleadoRequest request);
    EmpleadoFullResponse actualizarEmpleado(Long id, EmpleadoRequest request);
    void desactivarEmpleado(Long id);
    void reactivarEmpleado(Long id);
}
