package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria,Long> {
    List<Categoria> findByCategoriaPadreId(Long categoriaPadreId);
    Optional<Categoria> findByDenominacion (String denominacion);
}
