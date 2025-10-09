package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
