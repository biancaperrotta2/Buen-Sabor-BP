package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ClienteRepository extends BaseRepository<Cliente, Long> {

    // Tabla de admin "Clientes": búsqueda libre por nombre, apellido o email.
    @Query("SELECT c FROM Cliente c WHERE :busqueda = '' " +
            "OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<Cliente> buscar(@Param("busqueda") String busqueda, Pageable pageable);

    /**
     * Busca un cliente por su dirección de correo electrónico.
     * Fundamental para identificar al usuario desde el SecurityContext (JWT).
     */
    Optional<Cliente> findByEmail(String email);

    /**
     * Busca un cliente por su nombre de usuario de login.
     * Es lo que realmente identifica al usuario autenticado: el subject del JWT
     * es Usuario.usuario (ver TokenService), no el email.
     */
    Optional<Cliente> findByUsuario(String usuario);

    /**
     * Buscar clientes por apellido 
     * (Útil para filtros de administración)
     */
    List<Cliente> findByApellidoContainingIgnoreCase(String apellido);
}
