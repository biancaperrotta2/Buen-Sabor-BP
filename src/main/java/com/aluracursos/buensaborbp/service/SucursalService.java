package com.aluracursos.buensaborbp.service;

import java.util.List;
import com.aluracursos.buensaborbp.dto.SucursalRequest;
import com.aluracursos.buensaborbp.dto.SucursalResponse;
import com.aluracursos.buensaborbp.entity.Sucursal;

public interface SucursalService extends BaseService<Sucursal, Long> {

    List<SucursalResponse> listarActivas();

    boolean estaAbierta(Long id);

    SucursalResponse crearSucursal(SucursalRequest request);

    SucursalResponse actualizarSucursal(Long id, SucursalRequest request);

    void eliminarLogico(Long id);
}