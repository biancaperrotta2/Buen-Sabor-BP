package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Cliente;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ClienteRepository extends BaseRepository<Cliente, Long> {

    /**
     * Busca un cliente por su dirección de correo electrónico.
     * Fundamental para identificar al usuario desde el SecurityContext (JWT).
     */
    Optional<Cliente> findByEmail(String email);

    /**
     * Buscar clientes por apellido 
     * (Útil para filtros de administración)
     */
    List<Cliente> findByApellidoContainingIgnoreCase(String apellido);
}
