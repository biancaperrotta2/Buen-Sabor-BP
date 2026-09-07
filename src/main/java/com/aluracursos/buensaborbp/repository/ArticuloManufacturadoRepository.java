package com.aluracursos.buensaborbp.repository;

import com.aluracursos.buensaborbp.entity.ArticuloManufacturado;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticuloManufacturadoRepository extends JpaRepository <ArticuloManufacturado, Long> {

    // El catálogo (público o de admin) muestra platos finales, nunca semielaborados
    // (esos tienen su propia pantalla — ver findAllByEsSemielaboradoTrue).
    Page<ArticuloManufacturado> findAllByActivoTrueAndEsSemielaboradoFalse(PageRequest of);

    Page<ArticuloManufacturado> findAllByEsSemielaboradoFalse(PageRequest of);

    Page<ArticuloManufacturado> findAllByEsSemielaboradoTrue(PageRequest of);
}
