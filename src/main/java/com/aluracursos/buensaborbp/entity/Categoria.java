package com.aluracursos.buensaborbp.entity;

import com.aluracursos.buensaborbp.entity.Enums.TipoCategoria;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long id;

    @Column(nullable = false)
    private String denominacion;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    // varchar con default explícito: con ddl-auto=update, Hibernate crearía un ENUM nativo de
    // MySQL y dejaría las filas existentes sin valor válido (ver "Caveat" en CLAUDE.md).
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) not null default 'PRODUCTO'")
    private TipoCategoria tipo = TipoCategoria.PRODUCTO;

    // Fuera de equals/hashCode/toString: con @Data, el hashCode de una hija incluía al padre y
    // el del padre a sus subcategorías, así que cargar el Set de subcategorías recalculaba el
    // hashCode del propio Set a medio cargar (ConcurrentModificationException, 500 en cualquier
    // categoría con subcategorías, ej. Bebidas).
    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_padre_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Categoria categoriaPadre;

    @OneToMany(mappedBy = "categoriaPadre", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Categoria> subcategorias;

    // Insumo para elaborar / semielaborado -> INSUMO; plato, promoción o insumo que se vende
    // directo -> PRODUCTO. Se valida acá (no solo en los <select> del front) para que tampoco
    // entre mal por la API. "queSeGuarda" arma el mensaje, ej. "un insumo para elaborar".
    public void exigirTipo(TipoCategoria esperado, String queSeGuarda) {
        TipoCategoria actual = tipo != null ? tipo : TipoCategoria.PRODUCTO;
        if (actual != esperado) {
            throw new IllegalArgumentException("La categoría \"" + denominacion + "\" es de "
                    + nombreTipo(actual) + "; " + queSeGuarda + " va en una categoría de " + nombreTipo(esperado) + ".");
        }
    }

    private static String nombreTipo(TipoCategoria t) {
        return t == TipoCategoria.INSUMO ? "Insumos" : "Productos";
    }
}
