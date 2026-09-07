package com.aluracursos.buensaborbp.entity.Enums;

public enum Estado {
    PENDIENTE,    // Recién ingresado, el cajero aún no lo aceptó
    EN_PROCESO,   // El cajero lo aceptó y el cocinero lo tiene en las hornallas
    DEMORADO,     // El cocinero pospuso la hora estimada de entrega (ver Pedido.demorar())
    PREPARADO,    // (Tu "LISTO") Ya está en el mostrador esperando al delivery o cliente
    EN_CAMINO,    // (Solo si es Delivery) El repartidor ya lo tiene
    ENTREGADO,    // Fin del ciclo: El cliente ya tiene su comida
    CANCELADO     //El cliente o el negocio anulo el pedido
}
