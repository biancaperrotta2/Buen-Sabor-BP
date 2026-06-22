package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface EmpresaRepository extends JpaRepository<Empresa,Long> {
    boolean existsByCuit(Long cuit);
    
    Optional<Empresa> findByNombre(String nombre);
}
