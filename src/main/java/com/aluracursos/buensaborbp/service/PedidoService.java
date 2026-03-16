package com.aluracursos.buensaborbp.service;

import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.aluracursos.buensaborbp.entity.DetallePedido;
import com.aluracursos.buensaborbp.entity.Pedido;
import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.entity.Enums.TipoEnvio;
import com.aluracursos.buensaborbp.repository.PedidoRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio para la gestión de pedidos.
 * Proporciona operaciones relacionadas con pedidos, incluyendo cálculo de tiempos y gestión de estados.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PedidoService {
    
    private final PedidoRepository pedidoRepository;

    /**
     * Calcula el tiempo estimado de finalización de un pedido.
     * Considera el tiempo de preparación de cada artículo y el tiempo de envío si es delivery.
     * 
     * @param pedido Pedido para el cual calcular el tiempo
     * @return Tiempo estimado de finalización
     */
    public LocalTime calcularTiempoEstimadoFinalizacion(Pedido pedido) {
        if (pedido == null || pedido.getDetallePedidos() == null || pedido.getDetallePedidos().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un detalle");
        }

        int tiempoTotalMinutos = 0;
        int tiempoMaximoArticulo = 0;

        // Calcular el tiempo máximo de preparación entre todos los artículos
        for (DetallePedido detalle : pedido.getDetallePedidos()) {
            if (detalle.getArticulo() != null && detalle.getArticulo().getTiempoEstimadoMinutos() != null) {
                int tiempoArticulo = detalle.getArticulo().getTiempoEstimadoMinutos();
                tiempoMaximoArticulo = Math.max(tiempoMaximoArticulo, tiempoArticulo);
            }
        }

        tiempoTotalMinutos = tiempoMaximoArticulo;

        // Agregar tiempo de envío si es delivery (estimado: 20 minutos)
        if (pedido.getTipoEnvio() == TipoEnvio.DELIVERY) {
            tiempoTotalMinutos += 20;
        }

        LocalTime horaActual = LocalTime.now();
        return horaActual.plusMinutes(tiempoTotalMinutos);
    }

    /**
     * Obtiene un pedido por su ID.
     * 
     * @param id ID del pedido
     * @return Pedido encontrado
     * @throws EntityNotFoundException si no se encuentra el pedido
     */
    public Pedido findById(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el pedido con ID: " + id));
    }

    /**
     * Obtiene todos los pedidos.
     * 
     * @return Lista de todos los pedidos
     */
    public List<Pedido> findAll() {
        return pedidoRepository.findAll();
    }

    /**
     * Obtiene los pedidos de un cliente específico.
     * 
     * @param clienteId ID del cliente
     * @return Lista de pedidos del cliente
     */
    public List<Pedido> findByClienteId(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId);
    }

    /**
     * Obtiene los pedidos por estado.
     * 
     * @param estado Estado del pedido
     * @return Lista de pedidos con el estado especificado
     */
    public List<Pedido> findByEstadoPedido(Estado estado) {
        return pedidoRepository.findByEstadoPedido(estado);
    }

    /**
     * Guarda un pedido y calcula automáticamente los totales y tiempo estimado.
     * 
     * @param pedido Pedido a guardar
     * @return Pedido guardado
     */
    public Pedido save(Pedido pedido) {
        // Calcular totales
        pedido.calcularTotal();
        pedido.calcularCostoTotal();
        
        // Calcular tiempo estimado de finalización
        pedido.setHoraEstimadaFinalizacion(calcularTiempoEstimadoFinalizacion(pedido));
        
        return pedidoRepository.save(pedido);
    }

    /**
     * Actualiza el estado de un pedido.
     * 
     * @param pedidoId ID del pedido
     * @param nuevoEstado Nuevo estado del pedido
     * @return Pedido actualizado
     */
    public Pedido actualizarEstado(Long pedidoId, Estado nuevoEstado) {
        Pedido pedido = findById(pedidoId);
        pedido.setEstadoPedido(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    /**
     * Actualiza el estado de pago de un pedido.
     * 
     * @param pedidoId ID del pedido
     * @param nuevoEstadoPago Nuevo estado de pago
     * @return Pedido actualizado
     */
    public Pedido actualizarEstadoPago(Long pedidoId, Estado nuevoEstadoPago) {
        Pedido pedido = findById(pedidoId);
        pedido.setEstadoPago(nuevoEstadoPago);
        return pedidoRepository.save(pedido);
    }
}
