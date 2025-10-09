package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa,Long> {
}
