package com.aluracursos.buensaborbp.dto;

import java.util.List;

public record EmpresaResponse(
    Long id,
    String nombre,
    String razonSocial,
    Long cuit,
    List<SucursalResponse> sucursales
) {}

