package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.entity.Pedido;
import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.dto.PedidoBaseResponse;
import com.aluracursos.buensaborbp.dto.PedidoCajaResponse;
import com.aluracursos.buensaborbp.dto.PedidoCocinaResponse;
import com.aluracursos.buensaborbp.dto.PedidoEntregaResponse;
import com.aluracursos.buensaborbp.dto.PedidoFullResponse;
import com.aluracursos.buensaborbp.dto.PedidoRequest;
import com.aluracursos.buensaborbp.dto.PreferenciaPagoResponse;
import java.util.List;

public interface PedidoService extends BaseService<Pedido, Long> {
    PedidoFullResponse crearPedido(PedidoRequest request);
    PedidoFullResponse buscarPedidoPorId(Long id);
    PedidoFullResponse cambiarEstado(Long id, Estado nuevoEstado);

    PedidoFullResponse rechazar(Long id, String motivo);

    // Retorna la lista optimizada para el Cocinero
    List<PedidoCocinaResponse> listarParaCocina();

    List<PedidoBaseResponse> historialCliente(Long clienteId);

    // Crea la preferencia de pago en Mercado Pago para este pedido y devuelve el link de checkout.
    PreferenciaPagoResponse crearPreferenciaPago(Long pedidoId);

    // Llamado por el webhook: consulta el pago en Mercado Pago y actualiza el estado del pedido.
    void procesarNotificacionPago(Long paymentId);
    com.aluracursos.buensaborbp.dto.VerificacionPagoResponse verificarPago(Long pedidoId);

    // --- Flujo de repartidor ---

    // Pedidos PREPARADO + DELIVERY que todavía no tomó ningún repartidor.
    List<PedidoEntregaResponse> listarDisponiblesParaEnvio();

    List<PedidoEntregaResponse> misEntregas(Long repartidorId);

    // El repartidor autenticado toma un pedido del pool: lo asigna a sí mismo y pasa a EN_CAMINO.
    PedidoFullResponse tomarPedido(Long pedidoId, Long repartidorId);

    // Cierra la entrega: EN_CAMINO -> ENTREGADO. Solo el repartidor asignado (o un admin).
    PedidoFullResponse marcarEntregado(Long pedidoId);

    // --- Flujo de cajero ---

    // Pedidos recién ingresados que el cajero todavía no aceptó (cualquier tipo de envío).
    List<PedidoCajaResponse> listarPendientes();

    // Pedidos PREPARADO + TAKE_AWAY: listos para entregar y cobrar en el mostrador.
    List<PedidoCajaResponse> listarParaEntregarEnLocal();

    // Confirma que se cobró en efectivo (cajero en el mostrador o repartidor en la puerta).
    // Paso explícito y separado de marcar la entrega; un pedido con Mercado Pago nunca pasa
    // por acá, ese pago lo confirma el webhook. Si el pedido ya está ENTREGADO, factura sola.
    PedidoFullResponse confirmarCobroEfectivo(Long pedidoId);
}