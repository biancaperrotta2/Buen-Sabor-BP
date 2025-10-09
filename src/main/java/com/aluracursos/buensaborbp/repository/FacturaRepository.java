package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacturaRepository extends JpaRepository<Factura,Long> {
}
