package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.entity.Ciudad;
import com.aluracursos.buensaborbp.entity.Cliente;
import com.aluracursos.buensaborbp.entity.Domicilio;
import com.aluracursos.buensaborbp.entity.Enums.Rol;
import com.aluracursos.buensaborbp.mapper.DomicilioMapper;
import com.aluracursos.buensaborbp.repository.CiudadRepository;
import com.aluracursos.buensaborbp.repository.ClienteRepository;
import com.aluracursos.buensaborbp.repository.DomicilioRepository;
import com.aluracursos.buensaborbp.infra.security.FirebaseUserMirrorService;
import com.aluracursos.buensaborbp.mapper.ClienteMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.aluracursos.buensaborbp.dto.ClienteFullResponse;
import com.aluracursos.buensaborbp.dto.ClienteRequest;
import com.aluracursos.buensaborbp.dto.ClienteBaseResponse;
import com.aluracursos.buensaborbp.dto.DomicilioBaseResponse;
import com.aluracursos.buensaborbp.dto.DomicilioFullResponse;
import com.aluracursos.buensaborbp.dto.DomicilioRequest;
import com.aluracursos.buensaborbp.dto.PageResponse;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import com.aluracursos.buensaborbp.service.ClienteService;

@Service
public class ClienteServiceImpl extends BaseServiceImpl<Cliente, Long> implements ClienteService{
    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final PasswordEncoder passwordEncoder;
    private final DomicilioRepository domicilioRepository;
    private final DomicilioMapper domicilioMapper;
    private final CiudadRepository ciudadRepository;
    private final FirebaseUserMirrorService firebaseUserMirrorService;

    public ClienteServiceImpl(ClienteRepository clienteRepository, ClienteMapper clienteMapper, PasswordEncoder passwordEncoder,
                               DomicilioRepository domicilioRepository, DomicilioMapper domicilioMapper, CiudadRepository ciudadRepository,
                               FirebaseUserMirrorService firebaseUserMirrorService) {
        super(clienteRepository);
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
        this.passwordEncoder = passwordEncoder;
        this.domicilioRepository = domicilioRepository;
        this.domicilioMapper = domicilioMapper;
        this.ciudadRepository = ciudadRepository;
        this.firebaseUserMirrorService = firebaseUserMirrorService;
    }

    @Override
    @Transactional
    public ClienteFullResponse crearCliente(ClienteRequest request) {
        Cliente cliente = clienteMapper.toEntity(request);
        // El rol nunca lo elige quien se autorregistra; siempre CLIENTE.
        cliente.setRol(Rol.CLIENTE);
        cliente.setPassword(passwordEncoder.encode(request.password()));
        // Se crea con la contraseña en texto plano (todavía no se guardó nada), para que
        // exista una cuenta espejo en Firebase con la misma clave que el cliente eligió acá.
        cliente.setFirebaseUid(firebaseUserMirrorService.crearEspejo(
                request.email(), request.password(), cliente.getNombre() + " " + cliente.getApellido()));
        cliente = clienteRepository.save(cliente);

        return clienteMapper.toFullResponse(cliente);
    }

    @Override
    @Transactional
    public ClienteFullResponse actualizarCliente(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
        clienteMapper.updateEntity(request, cliente);
        cliente = clienteRepository.save(cliente);
        return clienteMapper.toFullResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteBaseResponse buscarClienteID(Long id) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
        return clienteMapper.toBaseResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClienteBaseResponse> mostrarClientes(String busqueda, Pageable pageable) {
        String filtro = busqueda != null ? busqueda.trim() : "";
        return PageResponse.de(clienteRepository.buscar(filtro, pageable), clienteMapper::toBaseResponse);
    }

    @Override
    @Transactional
    public void desactivarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    @Override
    @Transactional
    public void reactivarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
        cliente.setActivo(true);
        clienteRepository.save(cliente);
    }

    @Override
    @Transactional
    public DomicilioFullResponse agregarDomicilio(Long clienteId, DomicilioRequest request) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));

        Ciudad ciudad = ciudadRepository.findById(request.idCiudad())
                .orElseThrow(() -> new EntityNotFoundException("La ciudad seleccionada no existe"));

        Domicilio domicilio = domicilioMapper.toEntity(request);
        domicilio.setCiudad(ciudad);
        domicilio.setActivo(true);
        domicilio = domicilioRepository.save(domicilio);

        if (cliente.getDomicilio() == null) {
            cliente.setDomicilio(new ArrayList<>());
        }
        cliente.getDomicilio().add(domicilio);
        clienteRepository.save(cliente);

        return domicilioMapper.toFullResponse(domicilio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomicilioBaseResponse> misDomicilios(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
        return domicilioMapper.toBaseResponseList(cliente.getDomicilio());
    }
}
