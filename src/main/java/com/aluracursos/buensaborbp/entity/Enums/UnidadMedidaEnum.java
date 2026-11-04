package com.aluracursos.buensaborbp.entity.Enums;

import java.text.Normalizer;
import java.util.List;

public enum UnidadMedidaEnum {
    GR(0.001, "Gramos", "gr", List.of("gramo", "gramos", "gr", "g", "gra")), // 1000 gramos = 1kg
    KG(1.0, "Kilos", "kg", List.of("kilo", "kilos", "kilogramo", "kilogramos", "kg", "kil")),
    ML(0.001, "Mililitros", "ml", List.of("mililitro", "mililitros", "ml", "mil")),
    L(1.0, "Litros", "lt", List.of("litro", "litros", "l", "lt", "lit")),
    UNIDAD(1.0, "Unidad", "un", List.of("unidad", "unidades", "un", "uni"));

    private final double factor;
    private final String denominacion;
    private final String abreviatura;
    private final List<String> alias;

    UnidadMedidaEnum(double factor, String denominacion, String abreviatura, List<String> alias) {
        this.factor = factor;
        this.denominacion = denominacion;
        this.abreviatura = abreviatura;
        this.alias = alias;
    }

    public double getFactor() {
        return factor;
    }

    public String getDenominacion() {
        return denominacion;
    }

    public String getAbreviatura() {
        return abreviatura;
    }

    // El catálogo de unidades que ve el admin (tabla unidad_medida) es texto libre; el stock
    // en cambio se convierte con este enum. Se resuelve por nombre y, si no coincide, por abreviatura.
    public static UnidadMedidaEnum desdeCatalogo(String denominacion, String abreviatura) {
        UnidadMedidaEnum porNombre = buscar(denominacion);
        if (porNombre != null) return porNombre;
        UnidadMedidaEnum porAbreviatura = buscar(abreviatura);
        if (porAbreviatura != null) return porAbreviatura;
        throw new IllegalArgumentException("Unidad de medida no soportada: \"" + denominacion
                + "\". Use una de: Gramos, Kilos, Mililitros, Litros o Unidad");
    }

    private static UnidadMedidaEnum buscar(String texto) {
        if (texto == null) return null;
        String normalizado = Normalizer.normalize(texto.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        for (UnidadMedidaEnum unidad : values()) {
            if (unidad.alias.contains(normalizado)) return unidad;
        }
        return null;
    }
}
