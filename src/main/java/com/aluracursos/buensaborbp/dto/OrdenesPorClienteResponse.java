package com.aluracursos.buensaborbp.dto;

public record OrdenesPorClienteResponse(
    Long idCliente,
    String nombreCliente,
    Long cantidadOrdenes
) {}
