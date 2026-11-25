package com.aluracursos.buensaborbp.dto;

import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.entity.Enums.EstadoPago;
import com.aluracursos.buensaborbp.entity.Enums.FormaPago;
import com.aluracursos.buensaborbp.entity.Enums.TipoEnvio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

// Todo lo que muestra el diálogo de una orden en Órdenes y Cocina (figuras 33 a 38): pestañas
// Detalles (cliente, envío, historial de estados, pago) y Productos (aclaraciones, ítems), más
// los estados a los que el usuario que lo pide puede pasarla.
public record PedidoGestionResponse(
    Long id,
    LocalDate fechaPedido,
    LocalTime horaEstimadaFinalizacion,
    Estado estado,
    EstadoPago estadoPago,
    FormaPago formaPago,
    TipoEnvio tipoEnvio,
    BigDecimal total,
    BigDecimal gastosEnvio,
    BigDecimal descuento,
    // Dirección completa para el mapa ("Calle 123, Ciudad"); null si es retiro en el local.
    String direccionEnvio,
    Cliente cliente,
    Repartidor repartidor,
    List<CambioEstado> historial,
    Pago pago,
    String aclaraciones,
    List<ItemCocinaResponse> items,
    Long idFactura,
    String motivoCancelacion,
    Boolean reintegroPendiente,
    List<Estado> estadosPermitidos
) {
    public record Cliente(String nombre, String email, String telefono) {}

    public record Repartidor(String nombre, String telefono) {}

    public record CambioEstado(Estado estado, LocalDateTime fecha, String usuario) {}

    // transaccion: id del pago en Mercado Pago o de la orden cripto; null si es efectivo.
    public record Pago(String transaccion, String metodo, BigDecimal monto, LocalDateTime fecha) {}
}
