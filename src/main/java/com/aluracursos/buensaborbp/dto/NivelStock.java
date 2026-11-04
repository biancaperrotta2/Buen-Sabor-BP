package com.aluracursos.buensaborbp.dto;

// Clasificación de disponibilidad para el gráfico de Stock del dashboard de admin,
// según la misma regla de "Disponibilidad" (semáforo verde/amarillo/rojo) usada en
// Catálogo/Semielaborados/Insumos: suficiente si llega al máximo o está a más de 30
// unidades del mínimo; últimas unidades si está por debajo de esa línea pero todavía
// por encima del mínimo; agotado si ya llegó o cayó por debajo del mínimo.
public enum NivelStock {
    SUFICIENTE,
    ULTIMAS,
    AGOTADO;

    // "Últimas unidades" hasta 30 unidades por encima del mínimo; de ahí para arriba (o al
    // tope del máximo) ya es "Stock suficiente". Regla tomada de la consigna de referencia.
    private static final double MARGEN_ULTIMAS_UNIDADES = 30.0;

    public static NivelStock de(Double stockActual, Double stockMinimo, Double stockMaximo) {
        double actual = stockActual != null ? stockActual : 0.0;
        double minimo = stockMinimo != null ? stockMinimo : 0.0;
        if (actual <= minimo) {
            return AGOTADO;
        }
        boolean llegaAlMaximo = stockMaximo != null && actual >= stockMaximo;
        if (llegaAlMaximo || actual > minimo + MARGEN_ULTIMAS_UNIDADES) {
            return SUFICIENTE;
        }
        return ULTIMAS;
    }
}
