package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpleadoRepository extends JpaRepository<Empleado,Long> {
}
