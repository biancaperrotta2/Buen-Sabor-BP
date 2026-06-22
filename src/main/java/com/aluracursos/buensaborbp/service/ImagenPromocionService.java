package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.ImagenPromocionResponse;
import com.aluracursos.buensaborbp.entity.ImagenPromocion;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;


public interface ImagenPromocionService extends BaseService<ImagenPromocion, Long> {
    ImagenPromocionResponse subirImagen(MultipartFile file);
    void eliminarImagen(Long id);
    List<ImagenPromocionResponse> buscarPorPromocion(Long promocionId);
}
