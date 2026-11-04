package com.aluracursos.buensaborbp.controller;

import com.aluracursos.buensaborbp.dto.ImagenSubidaResponse;
import com.aluracursos.buensaborbp.exception.ImagenNoSubidaException;
import com.aluracursos.buensaborbp.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

// Sube la foto a Cloudinary y devuelve su URL; el admin la asocia al producto al guardarlo.
@RestController
@RequestMapping("/imagenes")
@RequiredArgsConstructor
// El cocinero también sube fotos (de semielaborados y productos).
@PreAuthorize("hasAnyRole('ADMIN','COCINERO')")
public class ImagenController {

    private static final long TAMANIO_MAXIMO_BYTES = 5L * 1024 * 1024;
    private static final Set<String> TIPOS_PERMITIDOS = Set.of("image/jpeg", "image/png", "image/webp");

    private final CloudinaryService cloudinaryService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImagenSubidaResponse> subir(@RequestParam("archivo") MultipartFile archivo) {
        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("Elegí una imagen para subir");
        }
        if (!TIPOS_PERMITIDOS.contains(archivo.getContentType())) {
            throw new IllegalArgumentException("Formato no permitido. Usá una imagen JPG, PNG o WEBP");
        }
        if (archivo.getSize() > TAMANIO_MAXIMO_BYTES) {
            throw new IllegalArgumentException("La imagen supera el máximo de 5 MB");
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ImagenSubidaResponse(cloudinaryService.subirImagen(archivo)));
        } catch (IOException | RuntimeException e) {
            throw new ImagenNoSubidaException(e);
        }
    }
}
