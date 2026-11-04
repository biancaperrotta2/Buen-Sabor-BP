package com.aluracursos.buensaborbp.exception;

public class CuentaDesactivadaException extends RuntimeException {
    public CuentaDesactivadaException() {
        super("Tu cuenta está desactivada. Contactá al administrador.");
    }
}
