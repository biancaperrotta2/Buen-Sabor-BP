package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.ImagenPromocion;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImagenPromocionRepository extends BaseRepository<ImagenPromocion, Long> {
    List<ImagenPromocion> findByPromocionId(Long promocionId);
    void deleteByPromocionId(Long promocionId);
}