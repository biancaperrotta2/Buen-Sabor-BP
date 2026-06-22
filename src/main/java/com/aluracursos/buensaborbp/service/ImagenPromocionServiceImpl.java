package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.ImagenPromocionResponse;
import com.aluracursos.buensaborbp.entity.ImagenPromocion;
import com.aluracursos.buensaborbp.mapper.ImagenPromocionMapper;
import com.aluracursos.buensaborbp.repository.ImagenPromocionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.util.Map;
import java.io.IOException;
import java.util.List;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ImagenPromocionServiceImpl extends BaseServiceImpl<ImagenPromocion, Long> implements ImagenPromocionService {

    private final ImagenPromocionRepository repository;
    private final ImagenPromocionMapper mapper;
    private final Cloudinary cloudinary;

    public ImagenPromocionServiceImpl(ImagenPromocionRepository repository, 
                                      ImagenPromocionMapper mapper, 
                                      Cloudinary cloudinary) {
        super(repository);
        this.repository = repository;
        this.mapper = mapper;
        this.cloudinary = cloudinary;
    }

    @Override
    @Transactional
    public ImagenPromocionResponse subirImagen(MultipartFile archivo) {
        try {
            // Subida a la nube en carpeta específica
            Map uploadResult = cloudinary.uploader().upload(archivo.getBytes(), 
                ObjectUtils.asMap("folder", "buensabor/promociones"));

            String url = uploadResult.get("url").toString();
            String publicId = uploadResult.get("public_id").toString();

            ImagenPromocion nueva = ImagenPromocion.builder()
                .urlImagen(url)
                .denominacion(publicId)
                .build();

            return mapper.toResponse(repository.save(nueva));

        } catch (IOException e) {
            throw new RuntimeException("Error al subir imagen de promoción: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void eliminarImagen(Long id) {
        ImagenPromocion img = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Imagen de promoción no encontrada"));

        try {
            // 1. Borrar de la nube
            cloudinary.uploader().destroy(img.getDenominacion(), ObjectUtils.emptyMap());
            
            // 2. Borrar de la BD
            repository.delete(img);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo eliminar la imagen de la nube");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImagenPromocionResponse> buscarPorPromocion(Long promocionId) {
        return mapper.toResponseList(repository.findByPromocionId(promocionId));
    }
}