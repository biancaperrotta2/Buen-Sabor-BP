package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.entity.Cliente;
import com.aluracursos.buensaborbp.dto.ClienteRequest;
import com.aluracursos.buensaborbp.dto.ClienteFullResponse;
import com.aluracursos.buensaborbp.dto.ClienteBaseResponse;
import com.aluracursos.buensaborbp.dto.DomicilioBaseResponse;
import com.aluracursos.buensaborbp.dto.DomicilioFullResponse;
import com.aluracursos.buensaborbp.dto.DomicilioRequest;
import java.util.List;

public interface ClienteService extends BaseService<Cliente, Long> {
    ClienteFullResponse crearCliente(ClienteRequest request);
    ClienteFullResponse actualizarCliente(Long id, ClienteRequest request);
    ClienteBaseResponse buscarClienteID(Long id);
    List<ClienteBaseResponse> mostrarClientes();
    void desactivarCliente(Long id);

    // Crea un domicilio nuevo y lo agrega a la lista de domicilios guardados del cliente,
    // en un solo paso (lo necesita el checkout: un pedido solo puede usar un domicilio propio).
    DomicilioFullResponse agregarDomicilio(Long clienteId, DomicilioRequest request);

    List<DomicilioBaseResponse> misDomicilios(Long clienteId);
}
