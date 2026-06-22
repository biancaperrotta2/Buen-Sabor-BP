package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.entity.ImagenUsuario;
import com.aluracursos.buensaborbp.dto.ImagenUsuarioResponse;
import com.aluracursos.buensaborbp.mapper.ImagenUsuarioMapper;
import com.aluracursos.buensaborbp.repository.ImagenPersonaRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.Map;

@Service
public class ImagenUsuarioServiceImpl extends BaseServiceImpl<ImagenUsuario, Long> implements ImagenUsuarioService {

    private final ImagenPersonaRepository repository;
    private final ImagenUsuarioMapper mapper;
    private final Cloudinary cloudinary;

    public ImagenUsuarioServiceImpl(ImagenPersonaRepository repository,
                                    ImagenUsuarioMapper mapper,
                                    Cloudinary cloudinary) {
        super(repository);
        this.repository = repository;
        this.mapper = mapper;
        this.cloudinary = cloudinary;
    }

    @Override
    @Transactional
    public ImagenUsuarioResponse subirFotoPerfil(MultipartFile archivo) {
        try {
            // 1. Subir a la carpeta 'usuarios'
            Map uploadResult = cloudinary.uploader().upload(archivo.getBytes(),
                    ObjectUtils.asMap("folder", "buensabor/usuarios"));

            String url = uploadResult.get("url").toString();
            String publicId = uploadResult.get("public_id").toString();

            // 2. Creamos la entidad
            ImagenUsuario nueva = ImagenUsuario.builder()
                    .urlImagen(url)
                    .denominacion(publicId)
                    .build();

            return mapper.toResponse(repository.save(nueva));

        } catch (IOException e) {
            throw new RuntimeException("Error al subir foto de perfil: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void eliminarFotoPerfil(Long id) {
        ImagenUsuario img = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Imagen de usuario no encontrada"));

        try {
            // Borramos de la nube
            cloudinary.uploader().destroy(img.getDenominacion(), ObjectUtils.emptyMap());
            // Borramos de la BD
            repository.delete(img);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar la foto de perfil en la nube");
        }
    }
}