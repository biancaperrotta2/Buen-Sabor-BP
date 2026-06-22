package com.aluracursos.buensaborbp.entity.Enums;

public enum Estado {
    PENDIENTE,    // Recién ingresado, el cocinero aún no lo "aceptó"
    EN_PROCESO,   // El cocinero lo aceptó y está en las hornallas
    PREPARADO,    // (Tu "LISTO") Ya está en el mostrador esperando al delivery o cliente
    EN_CAMINO,    // (Solo si es Delivery) El repartidor ya lo tiene
    ENTREGADO,    // Fin del ciclo: El cliente ya tiene su comida
    CANCELADO     //El cliente o el negocio anulo el pedido
}
