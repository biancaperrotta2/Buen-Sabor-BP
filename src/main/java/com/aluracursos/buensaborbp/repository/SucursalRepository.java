package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Sucursal;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SucursalRepository extends BaseRepository<Sucursal, Long> {
    
    // Lista solo las que están operativas
    List<Sucursal> findByActivoTrue();

    // Por si a futuro tenés varias empresas
    List<Sucursal> findByEmpresaId(Long empresaId);
}
