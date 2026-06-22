package com.aluracursos.buensaborbp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Repositorio base para centralizar capacidades comunes a todos los repositorios.
 *
 * <p>Extiende {@link JpaRepository} para CRUD y {@link JpaSpecificationExecutor}
 * para búsquedas dinámicas con Specifications.</p>
 *
 * @param <T>  entidad
 * @param <ID> tipo de la PK
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
}

