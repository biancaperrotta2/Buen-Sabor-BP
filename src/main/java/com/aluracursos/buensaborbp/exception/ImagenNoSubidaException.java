package com.aluracursos.buensaborbp.exception;

public class ImagenNoSubidaException extends RuntimeException {
    public ImagenNoSubidaException(Throwable causa) {
        super("No se pudo subir la imagen. Intentá de nuevo en unos minutos.", causa);
    }
}
