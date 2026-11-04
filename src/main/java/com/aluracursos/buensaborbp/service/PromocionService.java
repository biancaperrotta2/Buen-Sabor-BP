package com.aluracursos.buensaborbp.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.aluracursos.buensaborbp.dto.PromocionRequest;
import com.aluracursos.buensaborbp.entity.Promocion;
import com.aluracursos.buensaborbp.dto.PageResponse;
import com.aluracursos.buensaborbp.dto.PromocionBaseResponse;
import com.aluracursos.buensaborbp.dto.PromocionFullResponse;


public interface PromocionService extends BaseService<Promocion, Long> {

    /**
     * Crea una nueva promoción calculando automáticamente costos y precios
     * basados en los artículos que la componen.
     */
    PromocionFullResponse crearPromocion(PromocionRequest request);

    /**
     * Actualiza una promoción existente, recalculando los valores 
     * si se modifican sus detalles.
     */
    PromocionFullResponse actualizarPromocion(Long id, PromocionRequest request);

    /**
     * Retorna el detalle completo para la vista de edición del Admin.
     */
    PromocionFullResponse buscarPorIdFull(Long id);

    /**
     * Lista las promociones vigentes (por fecha y hora) para las 
     * Cards del menú del cliente.
     */
    List<PromocionBaseResponse> listarParaMenu();

    /**
     * Lista todas las promociones (habilitadas o no) para la tabla del Admin.
     */
    List<PromocionFullResponse> listarTodoParaAdmin();

    /**
     * Igual que listarTodoParaAdmin pero paginado y con búsqueda, para la tabla del Admin
     * (mismo patrón de busqueda+Pageable que Empleados/Clientes/Insumos/Productos/Categorías).
     */
    PageResponse<PromocionBaseResponse> listarAdmin(String busqueda, Pageable pageable);

    /**
     * Cambia el estado de habilitación (Baja lógica) de la promoción.
     */
    void cambiarEstadoHabilitacion(Long id);
}