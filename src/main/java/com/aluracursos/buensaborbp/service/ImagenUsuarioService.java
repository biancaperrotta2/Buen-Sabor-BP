package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.entity.ImagenUsuario;
import com.aluracursos.buensaborbp.dto.ImagenUsuarioResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ImagenUsuarioService extends BaseService<ImagenUsuario, Long> {
    // Sube la foto y se encarga de reemplazar la anterior si existía
    ImagenUsuarioResponse subirFotoPerfil(MultipartFile file);

    void eliminarFotoPerfil(Long id);
}
