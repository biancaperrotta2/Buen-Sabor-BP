package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Articulo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface ArticuloRepository extends JpaRepository<Articulo, Long> {

    // Productos, insumos y promociones (todos son Articulo) asignados a estas categorías.
    long countByCategoria_IdIn(Collection<Long> categoriaIds);
}
