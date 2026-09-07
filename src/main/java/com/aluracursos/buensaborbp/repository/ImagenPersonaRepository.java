package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.ImagenUsuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ImagenPersonaRepository extends BaseRepository<ImagenUsuario, Long> {
    // Para encontrar la foto de un usuario específico.
    // El PK de Usuario es id_usuario, no id: no se puede derivar por nombre.
    @Query("SELECT i FROM ImagenUsuario i WHERE i.usuario.id_usuario = :usuarioId")
    Optional<ImagenUsuario> findByPersonaId(@Param("usuarioId") Long usuarioId);
}