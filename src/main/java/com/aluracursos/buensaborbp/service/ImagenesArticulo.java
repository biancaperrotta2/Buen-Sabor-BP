package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.ImagenArticuloRequest;
import com.aluracursos.buensaborbp.entity.Articulo;
import com.aluracursos.buensaborbp.entity.ImagenArticulo;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Fotos de un Articulo (productos e insumos; las promociones usan ImagenPromocion aparte).
@Component
public class ImagenesArticulo {

    private final CloudinaryService cloudinaryService;

    public ImagenesArticulo(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    // Deja las imágenes del artículo igual a la lista pedida: agrega las nuevas, quita las que ya
    // no están (y las borra de Cloudinary). Con null no se toca nada, así una edición que no
    // menciona las imágenes no las pierde.
    public void sincronizar(Articulo articulo, List<ImagenArticuloRequest> solicitadas) {
        if (solicitadas == null) return;

        Set<String> urlsPedidas = new LinkedHashSet<>();
        for (ImagenArticuloRequest pedida : solicitadas) {
            if (pedida.url() == null || pedida.url().isBlank()) continue;
            if (!pedida.url().startsWith("https://")) {
                throw new IllegalArgumentException("La URL de la imagen debe ser https");
            }
            urlsPedidas.add(pedida.url());
        }

        List<String> aEliminarDeCloudinary = articulo.getImagenes().stream()
                .map(ImagenArticulo::getUrlImagen)
                .filter(url -> !urlsPedidas.contains(url))
                .toList();
        articulo.getImagenes().removeIf(imagen -> !urlsPedidas.contains(imagen.getUrlImagen()));

        Set<String> yaGuardadas = new HashSet<>();
        articulo.getImagenes().forEach(imagen -> yaGuardadas.add(imagen.getUrlImagen()));
        for (String url : urlsPedidas) {
            if (yaGuardadas.contains(url)) continue;
            articulo.getImagenes().add(ImagenArticulo.builder()
                    .denominacion(articulo.getNombre())
                    .urlImagen(url)
                    .articulo(articulo)
                    .build());
        }

        aEliminarDeCloudinary.stream()
                .filter(url -> url.contains("res.cloudinary.com"))
                .forEach(cloudinaryService::eliminarImagen);
    }
}
