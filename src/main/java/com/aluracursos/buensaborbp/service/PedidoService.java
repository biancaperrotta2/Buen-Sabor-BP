package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.entity.Pedido;
import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.dto.PedidoBaseResponse;
import com.aluracursos.buensaborbp.dto.PedidoCocinaResponse;
import com.aluracursos.buensaborbp.dto.PedidoFullResponse;
import com.aluracursos.buensaborbp.dto.PedidoRequest;
import java.util.List;

public interface PedidoService extends BaseService<Pedido, Long> {
    PedidoFullResponse crearPedido(PedidoRequest request);
    PedidoFullResponse cambiarEstado(Long id, Estado nuevoEstado);
    
    // Retorna la lista optimizada para el Cocinero
    List<PedidoCocinaResponse> listarParaCocina(); 
    
    List<PedidoBaseResponse> historialCliente(Long clienteId);
}