package com.aluracursos.buensaborbp.service;

import org.springframework.stereotype.Service;
import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoFullResponse;
import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoBaseResponse;
import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoRequest;
import org.springframework.data.domain.Page;

@Service
public interface ArticuloManufacturadoService {
    Page<ArticuloManufacturadoBaseResponse> getAll(); //devuelve una página de manufacturados
    ArticuloManufacturadoFullResponse getById(Long id); //busca un manufacturado por su id
    ArticuloManufacturadoFullResponse crearManufacturado(ArticuloManufacturadoRequest request); //crea un nuevo manufacturado
    ArticuloManufacturadoFullResponse update(Long id, ArticuloManufacturadoRequest request); //actualiza un manufacturado creado
    void delete(Long id); //borra un manufacturado por su id?

    //  metodo adicional
    Page<ArticuloManufacturadoBaseResponse> getActivos(); //devuelve una página de manufacturados ACTIVOS
    Boolean verificarStockParaVenta(Long manufacturadoId, Integer cantidadDeseada);
}
