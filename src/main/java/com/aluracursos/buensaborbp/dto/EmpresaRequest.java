package com.aluracursos.buensaborbp.dto;

public record EmpresaRequest(
    String nombre,
    String razonSocial,
    Long cuit 
) {}