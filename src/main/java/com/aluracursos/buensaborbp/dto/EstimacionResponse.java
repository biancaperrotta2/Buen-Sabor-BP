package com.aluracursos.buensaborbp.dto;

import java.time.LocalTime;

public record EstimacionResponse(LocalTime horaEstimada, int minutos) {}
