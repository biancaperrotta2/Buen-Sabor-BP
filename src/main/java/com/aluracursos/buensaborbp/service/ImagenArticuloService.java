package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.ImagenArticuloResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import com.aluracursos.buensaborbp.entity.ImagenArticulo;


public interface ImagenArticuloService extends BaseService<ImagenArticulo, Long> {
    
    /**
     * Sube una imagen a Cloudinary y la guarda en la base de datos.
     * @param file Archivo enviado desde el frontend.
     * @return DTO con la URL de la nube y el ID de la base de datos.
     */
    ImagenArticuloResponse subirImagen(MultipartFile file);

    /**
     * Elimina la imagen de Cloudinary y el registro de la base de datos.
     * @param id ID de la entidad ImagenArticulo.
     */
    void eliminarImagen(Long id);
}