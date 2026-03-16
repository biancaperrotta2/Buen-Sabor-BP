package com.aluracursos.buensaborbp.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Servicio para la gestión de imágenes en Cloudinary.
 * Proporciona métodos para subir y eliminar imágenes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Sube una imagen a Cloudinary.
     * 
     * @param archivo Archivo de imagen a subir
     * @return URL segura de la imagen subida
     * @throws IOException si ocurre un error al leer el archivo o subirlo
     * @throws IllegalArgumentException si el archivo está vacío o es nulo
     */
    public String subirImagen(MultipartFile archivo) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        try {
            Map<?, ?> resultado = cloudinary.uploader().upload(
                    archivo.getBytes(), 
                    ObjectUtils.asMap("resource_type", "auto")
            );
            
            String url = resultado.get("secure_url").toString();
            log.info("Imagen subida exitosamente: {}", url);
            return url;
        } catch (IOException e) {
            log.error("Error al subir imagen a Cloudinary", e);
            throw new IOException("Error al subir la imagen: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina una imagen de Cloudinary usando su URL pública.
     * 
     * @param urlImagen URL pública de la imagen a eliminar
     * @return true si la eliminación fue exitosa, false en caso contrario
     */
    public boolean eliminarImagen(String urlImagen) {
        if (urlImagen == null || urlImagen.isEmpty()) {
            log.warn("URL de imagen vacía o nula");
            return false;
        }

        try {
            // Extraer el public_id de la URL
            String publicId = extraerPublicId(urlImagen);
            if (publicId == null) {
                log.warn("No se pudo extraer el public_id de la URL: {}", urlImagen);
                return false;
            }

            Map<?, ?> resultado = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String resultadoEliminacion = resultado.get("result").toString();
            
            boolean eliminado = "ok".equals(resultadoEliminacion);
            if (eliminado) {
                log.info("Imagen eliminada exitosamente: {}", publicId);
            } else {
                log.warn("No se pudo eliminar la imagen: {}", publicId);
            }
            
            return eliminado;
        } catch (Exception e) {
            log.error("Error al eliminar imagen de Cloudinary: {}", urlImagen, e);
            return false;
        }
    }

    /**
     * Extrae el public_id de una URL de Cloudinary.
     * 
     * @param url URL de la imagen
     * @return public_id extraído o null si no se puede extraer
     */
    private String extraerPublicId(String url) {
        try {
            // Formato típico: https://res.cloudinary.com/{cloud_name}/image/upload/v{version}/{public_id}.{extension}
            int index = url.lastIndexOf("/");
            if (index == -1) {
                return null;
            }
            
            String publicIdConExtension = url.substring(index + 1);
            // Remover la extensión
            int puntoIndex = publicIdConExtension.lastIndexOf(".");
            if (puntoIndex != -1) {
                return publicIdConExtension.substring(0, puntoIndex);
            }
            
            return publicIdConExtension;
        } catch (Exception e) {
            log.error("Error al extraer public_id de la URL: {}", url, e);
            return null;
        }
    }
}
