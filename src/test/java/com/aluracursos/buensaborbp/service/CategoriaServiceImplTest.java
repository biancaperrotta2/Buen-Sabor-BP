package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.CategoriaRequest;
import com.aluracursos.buensaborbp.entity.Categoria;
import com.aluracursos.buensaborbp.entity.Enums.TipoCategoria;
import com.aluracursos.buensaborbp.mapper.CategoriaMapper;
import com.aluracursos.buensaborbp.repository.ArticuloRepository;
import com.aluracursos.buensaborbp.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Reglas del tipo de categoría (PRODUCTO / INSUMO): default PRODUCTO, una subcategoría
// siempre hereda el tipo de su padre, y cambiar el tipo de un padre arrastra a sus hijas.
@ExtendWith(MockitoExtension.class)
class CategoriaServiceImplTest {

    @Mock private CategoriaRepository categoriaRepository;
    @Mock private CategoriaMapper categoriaMapper;
    @Mock private ArticuloRepository articuloRepository;

    private CategoriaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CategoriaServiceImpl(categoriaRepository, categoriaMapper, articuloRepository);
        lenient().when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(categoriaMapper.toEntity(any(CategoriaRequest.class)))
                .thenAnswer(inv -> Categoria.builder().denominacion(((CategoriaRequest) inv.getArgument(0)).denominacion()).build());
    }

    private Categoria guardada() {
        ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
        verify(categoriaRepository).save(captor.capture());
        return captor.getValue();
    }

    @Nested
    @DisplayName("crearCategoria")
    class Crear {
        @Test
        @DisplayName("Sin tipo en el request, queda como PRODUCTO")
        void sinTipo_esProducto() {
            service.crearCategoria(new CategoriaRequest("Pizzas", null));

            assertThat(guardada().getTipo()).isEqualTo(TipoCategoria.PRODUCTO);
        }

        @Test
        @DisplayName("Respeta el tipo INSUMO pedido")
        void conTipoInsumo_esInsumo() {
            service.crearCategoria(new CategoriaRequest("Lácteos", null, TipoCategoria.INSUMO));

            assertThat(guardada().getTipo()).isEqualTo(TipoCategoria.INSUMO);
        }

        @Test
        @DisplayName("Una subcategoría hereda el tipo del padre aunque el request pida otro")
        void subcategoria_heredaTipoDelPadre() {
            Categoria lacteos = Categoria.builder().id(10L).denominacion("Lácteos").tipo(TipoCategoria.INSUMO).build();
            when(categoriaRepository.findById(10L)).thenReturn(Optional.of(lacteos));

            service.crearCategoria(new CategoriaRequest("Quesos", 10L, TipoCategoria.PRODUCTO));

            assertThat(guardada().getTipo()).isEqualTo(TipoCategoria.INSUMO);
        }
    }

    @Nested
    @DisplayName("actualizarCategoria")
    class Actualizar {
        @Test
        @DisplayName("Cambiar el tipo de un padre lo aplica también a sus subcategorías")
        void cambiarTipoDelPadre_arrastraALasHijas() {
            Categoria hija = Categoria.builder().id(2L).denominacion("Quesos").tipo(TipoCategoria.PRODUCTO).build();
            Set<Categoria> hijas = new HashSet<>();
            hijas.add(hija);
            Categoria padre = Categoria.builder().id(1L).denominacion("Lácteos")
                    .tipo(TipoCategoria.PRODUCTO).subcategorias(hijas).build();
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(padre));

            service.actualizarCategoria(1L, new CategoriaRequest("Lácteos", null, TipoCategoria.INSUMO));

            assertThat(padre.getTipo()).isEqualTo(TipoCategoria.INSUMO);
            assertThat(hija.getTipo()).isEqualTo(TipoCategoria.INSUMO);
        }

        @Test
        @DisplayName("Con artículos cargados (también en subcategorías), no deja cambiar el tipo")
        void conArticulos_noDejaCambiarElTipo() {
            Categoria hija = Categoria.builder().id(2L).denominacion("Gaseosas").tipo(TipoCategoria.PRODUCTO).build();
            Set<Categoria> hijas = new HashSet<>();
            hijas.add(hija);
            Categoria bebidas = Categoria.builder().id(1L).denominacion("Bebidas")
                    .tipo(TipoCategoria.PRODUCTO).subcategorias(hijas).build();
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(bebidas));
            when(articuloRepository.countByCategoria_IdIn(org.mockito.ArgumentMatchers.argThat(
                    ids -> ids.containsAll(java.util.List.of(1L, 2L))))).thenReturn(3L);

            IllegalStateException ex = org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                    () -> service.actualizarCategoria(1L, new CategoriaRequest("Bebidas", null, TipoCategoria.INSUMO)));

            assertThat(ex.getMessage()).contains("Bebidas").contains("3 artículos");
            assertThat(bebidas.getTipo()).isEqualTo(TipoCategoria.PRODUCTO);
            assertThat(hija.getTipo()).isEqualTo(TipoCategoria.PRODUCTO);
            verify(categoriaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Si el tipo no cambia, ni siquiera cuenta artículos (renombrar siempre se puede)")
        void mismoTipo_noCuentaArticulos() {
            Categoria lacteos = Categoria.builder().id(1L).denominacion("Lácteos").tipo(TipoCategoria.INSUMO).build();
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(lacteos));

            service.actualizarCategoria(1L, new CategoriaRequest("Lácteos y quesos", null, TipoCategoria.INSUMO));

            verifyNoInteractions(articuloRepository);
        }

        @Test
        @DisplayName("Sin tipo en el request, conserva el que tenía")
        void sinTipo_conservaElActual() {
            Categoria lacteos = Categoria.builder().id(1L).denominacion("Lácteos").tipo(TipoCategoria.INSUMO).build();
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(lacteos));

            service.actualizarCategoria(1L, new CategoriaRequest("Lácteos y quesos", null));

            assertThat(lacteos.getTipo()).isEqualTo(TipoCategoria.INSUMO);
        }
    }
}
