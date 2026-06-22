package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.entity.Cliente;
import com.aluracursos.buensaborbp.repository.ClienteRepository;
import com.aluracursos.buensaborbp.mapper.ClienteMapper;
import org.springframework.stereotype.Service;
import com.aluracursos.buensaborbp.dto.ClienteFullResponse;
import com.aluracursos.buensaborbp.dto.ClienteRequest;
import com.aluracursos.buensaborbp.dto.ClienteBaseResponse;
import java.util.List;
import jakarta.persistence.EntityNotFoundException;
import com.aluracursos.buensaborbp.service.ClienteService;

@Service
public class ClienteServiceImpl extends BaseServiceImpl<Cliente, Long> implements ClienteService{
    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    public ClienteServiceImpl(ClienteRepository clienteRepository, ClienteMapper clienteMapper) {
        super(clienteRepository);
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
    }

    @Override
    public ClienteFullResponse crearCliente(ClienteRequest request) {
        Cliente cliente = clienteMapper.toEntity(request);
        cliente = clienteRepository.save(cliente);
 
        return clienteMapper.toFullResponse(cliente);
    }

    @Override
    public ClienteFullResponse actualizarCliente(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
        clienteMapper.updateEntity(request, cliente);
        cliente = clienteRepository.save(cliente);
        return clienteMapper.toFullResponse(cliente);
    }

    @Override
    public ClienteBaseResponse buscarClienteID(Long id) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
        return clienteMapper.toBaseResponse(cliente);
    }

    @Override
    public List<ClienteBaseResponse> mostrarClientes() {
        List<Cliente> clientes = clienteRepository.findAll();
        return clienteMapper.toBaseResponseList(clientes);
    }

    @Override
    public void desactivarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }   
}
