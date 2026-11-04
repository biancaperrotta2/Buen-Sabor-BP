package com.aluracursos.buensaborbp.entity.Enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// desdeCatalogo() es el arreglo del bug "Creating an INSUMO failed in the backend" (2026-09-21):
// el catálogo de unidades (tabla UnidadMedida, texto libre) tenía que resolverse contra este
// enum, que es lo que de verdad controla la conversión de stock. Ver CLAUDE.md.
class UnidadMedidaEnumTest {

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @DisplayName("Resuelve por nombre exacto del catálogo")
    @CsvSource({
            "Gramos, GR",
            "Kilos, KG",
            "Mililitros, ML",
            "Litros, L",
            "Unidad, UNIDAD"
    })
    void resuelvePorNombreExacto(String nombreCatalogo, UnidadMedidaEnum esperado) {
        assertThat(UnidadMedidaEnum.desdeCatalogo(nombreCatalogo, "")).isEqualTo(esperado);
    }

    @Test
    @DisplayName("Resuelve por abreviatura cuando el nombre no coincide con ningún alias")
    void resuelvePorAbreviatura() {
        assertThat(UnidadMedidaEnum.desdeCatalogo("Kilogramo raro", "kg")).isEqualTo(UnidadMedidaEnum.KG);
    }

    @ParameterizedTest(name = "\"{0}\"")
    @DisplayName("Es insensible a mayúsculas/minúsculas y a los acentos")
    @CsvSource({
            "GRAMOS",
            "gramos",
            "GrÁmos",
            "gramo"
    })
    void esInsensibleAMayusculasYAcentos(String variante) {
        assertThat(UnidadMedidaEnum.desdeCatalogo(variante, "")).isEqualTo(UnidadMedidaEnum.GR);
    }

    @Test
    @DisplayName("Una unidad no soportada (ej. 'Docena') se rechaza con un mensaje claro")
    void unidadNoSoportada_lanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> UnidadMedidaEnum.desdeCatalogo("Docena", "doc"));

        assertThat(ex.getMessage()).contains("Docena").contains("no soportada");
    }

    @Test
    @DisplayName("Nombre y abreviatura vacíos/nulos también se rechazan, no explotan con NPE")
    void nombreYAbreviaturaNulos_lanzaExcepcionNoNpe() {
        assertThrows(IllegalArgumentException.class, () -> UnidadMedidaEnum.desdeCatalogo(null, null));
    }
}
