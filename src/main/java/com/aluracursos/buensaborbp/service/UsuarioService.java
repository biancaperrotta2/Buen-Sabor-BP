package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.entity.Usuario;
import java.util.List;

public interface UsuarioService {
    List<Usuario> listarTodos();
    Usuario buscarPorId(Long id);
    Usuario guardar(Usuario usuario);
    Usuario actualizar(Long id, Usuario usuario);
    void eliminar(Long id);
    Usuario buscarPorUsuario(String username); // Útil para cuando agregues seguridad
}