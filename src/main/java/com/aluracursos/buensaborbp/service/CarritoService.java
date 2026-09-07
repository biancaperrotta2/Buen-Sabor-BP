package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.AgregarItemCarritoRequest;
import com.aluracursos.buensaborbp.dto.CarritoResponse;

public interface CarritoService {
    CarritoResponse obtenerMiCarrito(Long clienteId);

    CarritoResponse agregarItem(Long clienteId, AgregarItemCarritoRequest request);

    CarritoResponse actualizarCantidad(Long clienteId, Long idArticulo, int cantidad);

    CarritoResponse eliminarItem(Long clienteId, Long idArticulo);

    void vaciarCarrito(Long clienteId);
}
