package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Empleado;
import com.aluracursos.buensaborbp.entity.Enums.Rol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmpleadoRepository extends JpaRepository<Empleado,Long> {

    // Tabla de admin "Empleados": búsqueda libre por nombre, apellido o email.
    // busqueda vacío/null trae todos (la condición se evalúa a true y no filtra nada).
    @Query("SELECT e FROM Empleado e WHERE :busqueda = '' " +
            "OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(e.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(e.email) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<Empleado> buscar(@Param("busqueda") String busqueda, Pageable pageable);

    // activo null cuenta como activo (ver Empleado.isEnabled).
    @Query("SELECT COUNT(e) FROM Empleado e WHERE e.rol = :rol AND (e.activo IS NULL OR e.activo = true)")
    long contarActivosPorRol(@Param("rol") Rol rol);
}
