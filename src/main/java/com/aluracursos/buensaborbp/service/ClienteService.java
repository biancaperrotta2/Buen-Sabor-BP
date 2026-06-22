package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.entity.Cliente;
import com.aluracursos.buensaborbp.dto.ClienteRequest;
import com.aluracursos.buensaborbp.dto.ClienteFullResponse;
import com.aluracursos.buensaborbp.dto.ClienteBaseResponse;
import java.util.List;

public interface ClienteService extends BaseService<Cliente, Long> {
    ClienteFullResponse crearCliente(ClienteRequest request);
    ClienteFullResponse actualizarCliente(Long id, ClienteRequest request);
    ClienteBaseResponse buscarClienteID(Long id);
    List<ClienteBaseResponse> mostrarClientes();
    void desactivarCliente(Long id);
}
