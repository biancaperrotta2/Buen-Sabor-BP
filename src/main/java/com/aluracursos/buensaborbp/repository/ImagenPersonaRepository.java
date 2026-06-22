package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.ImagenUsuario;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface ImagenPersonaRepository extends BaseRepository<ImagenUsuario, Long> {
    // Para encontrar la foto de un usuario específico
    Optional<ImagenUsuario> findByPersonaId(Long personaId);
}