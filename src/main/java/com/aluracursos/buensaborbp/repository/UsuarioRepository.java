package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsuario(String usuario);

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByOauth2Id(String oauth2Id);

    Optional<Usuario> findByFirebaseUid(String firebaseUid);
}
