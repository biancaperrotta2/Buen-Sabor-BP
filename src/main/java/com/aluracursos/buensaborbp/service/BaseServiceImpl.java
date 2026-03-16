package com.aluracursos.buensaborbp.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Implementación base para servicios que proporcionan operaciones CRUD básicas.
 * 
 * @param <T> Tipo de entidad
 * @param <ID> Tipo del identificador de la entidad
 */
public abstract class BaseServiceImpl<T, ID> implements BaseService<T, ID> {

    protected JpaRepository<T, ID> repository;

    public BaseServiceImpl(JpaRepository<T, ID> repository) {
        this.repository = repository;
    }

    /**
     * Obtiene todas las entidades.
     * 
     * @return Lista de todas las entidades
     */
    @Override
    public List<T> findAll() {
        return repository.findAll();
    }

    /**
     * Busca una entidad por su ID.
     * 
     * @param id ID de la entidad
     * @return Entidad encontrada
     * @throws EntityNotFoundException si no se encuentra la entidad
     */
    @Override
    public T findById(ID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el recurso con id: " + id));
    }

    /**
     * Crea una nueva entidad.
     * 
     * @param entity Entidad a crear
     * @return Entidad creada
     */
    @Override
    public T create(T entity) {
        return repository.save(entity);
    }

    /**
     * Actualiza una entidad existente.
     * 
     * @param id ID de la entidad a actualizar
     * @param entity Entidad con los datos actualizados
     * @return Entidad actualizada
     * @throws EntityNotFoundException si no se encuentra la entidad
     */
    @Override
    public T update(ID id, T entity) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("No se puede actualizar, no existe el recurso con id: " + id);
        }
        return repository.save(entity);
    }

    /**
     * Elimina una entidad por su ID.
     * 
     * @param id ID de la entidad a eliminar
     * @throws EntityNotFoundException si no se encuentra la entidad
     */
    @Override
    public void delete(ID id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("No se encontró el recurso con id: " + id);
        }
        repository.deleteById(id);
    }
}
