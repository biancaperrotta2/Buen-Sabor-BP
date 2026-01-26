package com.aluracursos.buensaborbp.entity.Enums;

public enum UnidadMedidaEnum {
    GR(0.001), // 1000 gramos = 1kg
    KG(1.0),
    ML(0.001),
    L(1.0),
    UNIDAD(1.0);

    private final double factor;

    UnidadMedidaEnum(double factor) {
        this.factor = factor;
    }

    public double getFactor() {
        return factor;
    }
}
