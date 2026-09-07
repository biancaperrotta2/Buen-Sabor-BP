package com.aluracursos.buensaborbp.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiValidationError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> errores
) {}
