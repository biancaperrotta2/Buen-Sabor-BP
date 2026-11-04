package com.aluracursos.buensaborbp.service;

import org.springframework.stereotype.Service;
import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoFullResponse;
import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoBaseResponse;
import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoRequest;
import com.aluracursos.buensaborbp.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public interface ArticuloManufacturadoService {
    Page<ArticuloManufacturadoBaseResponse> getAll(); //devuelve una página de manufacturados
    PageResponse<ArticuloManufacturadoBaseResponse> getAllAdmin(String busqueda, Pageable pageable); // para la tabla de admin, con búsqueda/paginado/orden reales
    ArticuloManufacturadoFullResponse getById(Long id); //busca un manufacturado por su id
    ArticuloManufacturadoFullResponse crearManufacturado(ArticuloManufacturadoRequest request); //crea un nuevo manufacturado
    ArticuloManufacturadoFullResponse update(Long id, ArticuloManufacturadoRequest request); //actualiza un manufacturado creado
    void delete(Long id); //borra un manufacturado por su id?

    //  metodo adicional
    Page<ArticuloManufacturadoBaseResponse> getActivos(); //devuelve una página de manufacturados ACTIVOS
    Page<ArticuloManufacturadoBaseResponse> getSemielaborados(); // solo los marcados esSemielaborado=true
    PageResponse<com.aluracursos.buensaborbp.dto.SemielaboradoAdminResponse> getSemielaboradosAdmin(String busqueda, Pageable pageable);
    Boolean verificarStockParaVenta(Long manufacturadoId, Integer cantidadDeseada);
}
