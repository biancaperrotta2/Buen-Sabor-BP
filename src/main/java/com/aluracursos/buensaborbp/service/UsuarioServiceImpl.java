package com.aluracursos.buensaborbp.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aluracursos.buensaborbp.entity.Usuario;
import com.aluracursos.buensaborbp.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public Usuario guardar(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public Usuario actualizar(Long id, Usuario datosActualizados) {
        Usuario usuarioExistente = buscarPorId(id);

        // Actualizamos los campos individuales básicos
        usuarioExistente.setNombre(datosActualizados.getNombre());
        usuarioExistente.setApellido(datosActualizados.getApellido());
        usuarioExistente.setUsuario(datosActualizados.getUsuario());
        usuarioExistente.setEmail(datosActualizados.getEmail());
        usuarioExistente.setTelefono(datosActualizados.getTelefono());
        usuarioExistente.setRol(datosActualizados.getRol());
        
        // Si el password cambia, se actualiza y se encripta
        if (datosActualizados.getPassword() != null && !datosActualizados.getPassword().isBlank()) {
            usuarioExistente.setPassword(passwordEncoder.encode(datosActualizados.getPassword()));
        }

        // Manejo de las imágenes de usuario (limpieza y sincronización)
        usuarioExistente.getImagenes().clear();
        if (datosActualizados.getImagenes() != null) {
            datosActualizados.getImagenes().forEach(img -> {
                img.setUsuario(usuarioExistente);
                usuarioExistente.getImagenes().add(img);
            });
        }

        return usuarioRepository.save(usuarioExistente);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuarioRepository.delete(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorUsuario(String username) {
        return usuarioRepository.findByUsuario(username)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con el username: " + username));
    }
}