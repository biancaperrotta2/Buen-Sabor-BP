package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Ciudad;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CiudadRepository extends BaseRepository<Ciudad, Long> {
    
    List<Ciudad> findByAltaTrue();
    List<Ciudad> findByProvinciaId(Long provinciaId);
} 

