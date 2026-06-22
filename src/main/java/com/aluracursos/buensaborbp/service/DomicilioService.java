package com.aluracursos.buensaborbp.service;

import java.util.List;

import com.aluracursos.buensaborbp.dto.DomicilioBaseResponse;
import com.aluracursos.buensaborbp.dto.DomicilioFullResponse;
import com.aluracursos.buensaborbp.dto.DomicilioRequest;
import com.aluracursos.buensaborbp.entity.Domicilio;

public interface DomicilioService extends BaseService<Domicilio, Long>{
//crear nuevo domicilio
DomicilioFullResponse crearDomicilio(DomicilioRequest request);
//actualizar domicilio/editar
DomicilioFullResponse actualizarDomicilio(Long id, DomicilioRequest request);
//buscar domicilio por id (nombre)
DomicilioBaseResponse buscarDomicilioID(Long id);
//mostrar todos los domicilios 
List<DomicilioBaseResponse> mostrarDomicilios();
//eliminar domicilio
void desactivarDomicilio(Long id);
}