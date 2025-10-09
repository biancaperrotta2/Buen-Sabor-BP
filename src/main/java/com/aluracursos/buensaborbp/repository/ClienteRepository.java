package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente,Long> {
}
