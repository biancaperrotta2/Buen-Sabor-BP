package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.entity.Empleado;
import com.aluracursos.buensaborbp.dto.EmpleadoBaseResponse;
import com.aluracursos.buensaborbp.dto.EmpleadoFullResponse;
import com.aluracursos.buensaborbp.dto.EmpleadoRequest;
import java.util.List;

public interface EmpleadoService extends BaseService<Empleado, Long>{
    EmpleadoBaseResponse buscarEmpleadoID(Long id);
    List<EmpleadoBaseResponse> mostrarEmpleados();
    EmpleadoFullResponse crearEmpleado(EmpleadoRequest request);
    EmpleadoFullResponse actualizarEmpleado(Long id, EmpleadoRequest request);
    void desactivarEmpleado(Long id);
}
