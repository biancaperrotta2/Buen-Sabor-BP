package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.ImagenArticulo;
import java.util.List;
import org.springframework.stereotype.Repository;


@Repository
public interface ImagenArticuloRepository extends BaseRepository<ImagenArticulo, Long> {

    /**
     * Busca todas las imágenes que pertenecen a un artículo específico.
     * Útil para cuando el frontend necesita renderizar el carrusel de fotos de un lomo.
     */
    List<ImagenArticulo> findByArticuloId(Long articuloId);

    /**
     * Permite borrar todas las imágenes de un artículo de una sola vez.
     * Útil si se elimina el producto y querés limpiar la base de datos.
     */
    void deleteByArticuloId(Long articuloId);
}
