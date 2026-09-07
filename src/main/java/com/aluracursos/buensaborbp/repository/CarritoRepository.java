package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Carrito;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoRepository extends BaseRepository<Carrito, Long> {

    // El PK de Cliente (heredado de Usuario) es id_usuario, no id: no se puede derivar por nombre.
    @Query("SELECT c FROM Carrito c WHERE c.cliente.id_usuario = :clienteId")
    Optional<Carrito> findByClienteId(@Param("clienteId") Long clienteId);
}
