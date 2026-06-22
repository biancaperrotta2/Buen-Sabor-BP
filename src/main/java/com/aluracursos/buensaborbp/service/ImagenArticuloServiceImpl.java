package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.entity.ImagenArticulo;
import com.aluracursos.buensaborbp.dto.ImagenArticuloResponse;
import com.aluracursos.buensaborbp.repository.ImagenArticuloRepository;
import com.aluracursos.buensaborbp.mapper.ImagenArticuloMapper;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;

import java.io.IOException;
import java.util.Map;

@Service
public class ImagenArticuloServiceImpl extends BaseServiceImpl<ImagenArticulo, Long> implements ImagenArticuloService {

    private final ImagenArticuloRepository repository;
    private final ImagenArticuloMapper mapper;
    private final Cloudinary cloudinary; 

    public ImagenArticuloServiceImpl(ImagenArticuloRepository repository, ImagenArticuloMapper mapper, Cloudinary cloudinary) {
        super(repository);
        this.repository = repository;
        this.mapper = mapper;
        this.cloudinary = cloudinary;
    }

    @Override
    @Transactional
    public ImagenArticuloResponse subirImagen(MultipartFile archivo) {
        try {
            // 1. Subir a Cloudinary
            // Map.of() es cómodo para pasar opciones como la carpeta
            Map uploadResult = cloudinary.uploader().upload(archivo.getBytes(), 
                ObjectUtils.asMap("folder", "buensabor/articulos"));

            // 2. Extraer los datos que nos devuelve la nube
            String url = uploadResult.get("url").toString();
            String publicId = uploadResult.get("public_id").toString(); // Importante para borrarla después

            // 3. Guardar en nuestra BD
            ImagenArticulo nueva = ImagenArticulo.builder()
                .urlImagen(url)
                .denominacion(publicId) // Usamos el public_id como nombre para poder borrarla luego
                .build();

            return mapper.toResponse(repository.save(nueva));

        } catch (IOException e) {
            throw new RuntimeException("Error al subir imagen a Cloudinary: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void eliminarImagen(Long id) {
        ImagenArticulo img = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada"));

        try {
            // Borrar de Cloudinary usando el publicId que guardamos en 'name'
            cloudinary.uploader().destroy(img.getDenominacion(), ObjectUtils.emptyMap());
            
            // Borrar de nuestra BD
            repository.delete(img);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar imagen en Cloudinary");
        }
    }
}