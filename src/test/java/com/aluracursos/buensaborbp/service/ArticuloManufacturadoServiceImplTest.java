package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoDetalleRequest;
import com.aluracursos.buensaborbp.dto.ArticuloManufacturadoRequest;
import com.aluracursos.buensaborbp.dto.CategoriaRequest;
import com.aluracursos.buensaborbp.dto.ImagenArticuloRequest;
import com.aluracursos.buensaborbp.entity.ArticuloInsumo;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturado;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturadoDetalle;
import com.aluracursos.buensaborbp.entity.Categoria;
import com.aluracursos.buensaborbp.entity.Enums.TipoCategoria;
import com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum;
import com.aluracursos.buensaborbp.entity.ImagenArticulo;
import com.aluracursos.buensaborbp.mapper.ArticuloManufacturadoMapper;
import com.aluracursos.buensaborbp.repository.ArticuloManufacturadoRepository;
import com.aluracursos.buensaborbp.repository.ArticuloRepository;
import com.aluracursos.buensaborbp.repository.CategoriaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

// Es el service más grande de los tres (ver CLAUDE.md, "Automated tests"): valida categoría,
// arma la receta (`construirDetalles`, componente debe ser insumo o semielaborado, nunca un
// plato final ni una promoción), decide los campos de stock según esSemielaborado
// (`aplicarConfiguracionStock`) y sincroniza fotos contra Cloudinary (`sincronizarImagenes`,
// mismo patrón duplicado en PromocionServiceImpl). Los tres métodos de arriba son privados:
// se ejercitan acá a través de crearManufacturado/update, que es como se usan en producción.
@ExtendWith(MockitoExtension.class)
class ArticuloManufacturadoServiceImplTest {

    @Mock private ArticuloManufacturadoRepository manufacturadoRepository;
    @Mock private ArticuloManufacturadoMapper manufacturadoMapper;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private ArticuloRepository articuloRepository;
    @Mock private CloudinaryService cloudinaryService;
    @Mock private RecalculoCostoProductos recalculoCostoProductos;

    private ArticuloManufacturadoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ArticuloManufacturadoServiceImpl(
                manufacturadoRepository, manufacturadoMapper, categoriaRepository, articuloRepository,
                new ImagenesArticulo(cloudinaryService),
                recalculoCostoProductos);
        lenient().when(manufacturadoRepository.save(any(ArticuloManufacturado.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(manufacturadoMapper.toEntity(any(ArticuloManufacturadoRequest.class)))
                .thenAnswer(inv -> mapearComoLoHaceElMapperReal(inv.getArgument(0)));
    }

    // Replica ArticuloManufacturadoMapper#toEntity: ignora imagenes/detalles (el service los
    // arma aparte), el resto se auto-mapea por nombre salvo nombre<-denominacion.
    private static ArticuloManufacturado mapearComoLoHaceElMapperReal(ArticuloManufacturadoRequest r) {
        return ArticuloManufacturado.builder()
                .nombre(r.denominacion())
                .descripcion(r.descripcion())
                .preparacion(r.preparacion())
                .precioVenta(r.precioVenta())
                .build();
    }

    private static ArticuloManufacturadoRequest platoFinalRequest(List<ArticuloManufacturadoDetalleRequest> detalles) {
        return new ArticuloManufacturadoRequest(
                "Pizza Muzzarella", false, null, null, null, null,
                "Pizza con muzzarella", BigDecimal.valueOf(5000), null, "Hornear 10 min",
                new CategoriaRequest("Pizzas", 3L), null, detalles);
    }

    private static ArticuloManufacturadoRequest semielaboradoRequest(Double stockActual, Double stockMinimo, Double stockMaximo) {
        return new ArticuloManufacturadoRequest(
                "Masa de pizza", true, UnidadMedidaEnum.KG, stockActual, stockMinimo, stockMaximo,
                "Masa base", BigDecimal.valueOf(2000), null, "Amasar y leudar",
                new CategoriaRequest("Semielaborados", 4L), null, null);
    }

    private static ArticuloInsumo insumoConCosto(double precioCosto, double stock) {
        return ArticuloInsumo.builder()
                .nombre("Queso").precioVenta(BigDecimal.TEN).precioCosto(BigDecimal.valueOf(precioCosto))
                .unidadMedidaEnum(UnidadMedidaEnum.KG)
                .stockActual(stock).stockMaximo(100.0).stockMinimo(1.0).stockReservado(0.0)
                .build();
    }

    private static ArticuloManufacturado semielaboradoConStock(double stock) {
        return ArticuloManufacturado.builder()
                .nombre("Masa").precioVenta(BigDecimal.TEN).precioCosto(BigDecimal.ONE)
                .unidadMedidaEnum(UnidadMedidaEnum.KG)
                .descripcion("d").preparacion("p").esSemielaborado(true)
                .stockActual(stock).stockMaximo(100.0).stockMinimo(1.0).stockReservado(0.0)
                .articuloManufacturadoDetalles(new ArrayList<>())
                .build();
    }

    private static ArticuloManufacturado platoFinal(String nombre, ArticuloManufacturadoDetalle... detalles) {
        ArticuloManufacturado plato = ArticuloManufacturado.builder()
                .nombre(nombre).precioVenta(BigDecimal.valueOf(5000))
                .unidadMedidaEnum(UnidadMedidaEnum.UNIDAD)
                .descripcion("d").preparacion("p").esSemielaborado(false)
                .articuloManufacturadoDetalles(new ArrayList<>(List.of(detalles)))
                .build();
        return plato;
    }

    @Nested
    @DisplayName("verificarStockParaVenta")
    class VerificarStockParaVenta {
        @Test
        @DisplayName("Plato inexistente lanza EntityNotFoundException")
        void platoInexistente_lanzaExcepcion() {
            when(manufacturadoRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> service.verificarStockParaVenta(1L, 2));
        }

        @Test
        @DisplayName("True cuando todos los componentes (insumo y semielaborado) alcanzan")
        void todosAlcanzan_esTrue() {
            ArticuloInsumo queso = insumoConCosto(4000, 10.0); // KG
            ArticuloManufacturado masa = semielaboradoConStock(10.0);
            ArticuloManufacturadoDetalle dQueso = ArticuloManufacturadoDetalle.builder().componente(queso).cantidad(0.2).build();
            ArticuloManufacturadoDetalle dMasa = ArticuloManufacturadoDetalle.builder().componente(masa).cantidad(0.3).build();
            ArticuloManufacturado plato = platoFinal("Pizza", dQueso, dMasa);
            when(manufacturadoRepository.findById(1L)).thenReturn(Optional.of(plato));

            assertThat(service.verificarStockParaVenta(1L, 3)).isTrue();
        }

        @Test
        @DisplayName("False cuando un componente insumo no alcanza")
        void insumoInsuficiente_esFalse() {
            ArticuloInsumo queso = insumoConCosto(4000, 0.1); // casi sin stock
            ArticuloManufacturadoDetalle dQueso = ArticuloManufacturadoDetalle.builder().componente(queso).cantidad(0.2).build();
            ArticuloManufacturado plato = platoFinal("Pizza", dQueso);
            when(manufacturadoRepository.findById(1L)).thenReturn(Optional.of(plato));

            assertThat(service.verificarStockParaVenta(1L, 5)).isFalse();
        }

        @Test
        @DisplayName("False cuando un componente semielaborado no alcanza")
        void semielaboradoInsuficiente_esFalse() {
            ArticuloManufacturado masa = semielaboradoConStock(0.1);
            ArticuloManufacturadoDetalle dMasa = ArticuloManufacturadoDetalle.builder().componente(masa).cantidad(0.3).build();
            ArticuloManufacturado plato = platoFinal("Pizza", dMasa);
            when(manufacturadoRepository.findById(1L)).thenReturn(Optional.of(plato));

            assertThat(service.verificarStockParaVenta(1L, 5)).isFalse();
        }
    }

    @Nested
    @DisplayName("crearManufacturado")
    class CrearManufacturado {
        @Test
        @DisplayName("Sin categoría, rechaza con IllegalArgumentException")
        void sinCategoria_lanzaExcepcion() {
            ArticuloManufacturadoRequest request = new ArticuloManufacturadoRequest(
                    "Pizza", false, null, null, null, null, "desc", BigDecimal.TEN, null, "prep", null, null, null);

            assertThrows(IllegalArgumentException.class, () -> service.crearManufacturado(request));
        }

        @Test
        @DisplayName("Categoría inexistente lanza EntityNotFoundException")
        void categoriaInexistente_lanzaExcepcion() {
            when(categoriaRepository.findById(3L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> service.crearManufacturado(platoFinalRequest(null)));
        }

        @Test
        @DisplayName("Componente inexistente lanza EntityNotFoundException")
        void componenteInexistente_lanzaExcepcion() {
            when(categoriaRepository.findById(3L)).thenReturn(Optional.of(Categoria.builder().build()));
            when(articuloRepository.findById(99L)).thenReturn(Optional.empty());
            List<ArticuloManufacturadoDetalleRequest> detalles = List.of(new ArticuloManufacturadoDetalleRequest(99L, 1.0));

            assertThrows(EntityNotFoundException.class, () -> service.crearManufacturado(platoFinalRequest(detalles)));
        }

        @Test
        @DisplayName("Un plato final como ingrediente es rechazado (no es insumo ni semielaborado)")
        void componentePlatoFinal_lanzaExcepcion() {
            when(categoriaRepository.findById(3L)).thenReturn(Optional.of(Categoria.builder().build()));
            ArticuloManufacturado otroPlatoFinal = platoFinal("Papas fritas");
            when(articuloRepository.findById(7L)).thenReturn(Optional.of(otroPlatoFinal));
            List<ArticuloManufacturadoDetalleRequest> detalles = List.of(new ArticuloManufacturadoDetalleRequest(7L, 1.0));

            assertThrows(IllegalArgumentException.class, () -> service.crearManufacturado(platoFinalRequest(detalles)));
        }

        @Test
        @DisplayName("Plato final válido: arma la receta, calcula costo, fuerza unidad UNIDAD y guarda")
        void platoFinalValido_seArmaYGuarda() {
            when(categoriaRepository.findById(3L)).thenReturn(Optional.of(Categoria.builder().build()));
            ArticuloInsumo queso = insumoConCosto(4000, 10.0);
            when(articuloRepository.findById(7L)).thenReturn(Optional.of(queso));
            List<ArticuloManufacturadoDetalleRequest> detalles = List.of(new ArticuloManufacturadoDetalleRequest(7L, 0.2));

            service.crearManufacturado(platoFinalRequest(detalles));

            ArgumentCaptor<ArticuloManufacturado> captor = ArgumentCaptor.forClass(ArticuloManufacturado.class);
            verify(manufacturadoRepository).save(captor.capture());
            ArticuloManufacturado guardado = captor.getValue();
            assertThat(guardado.getArticuloManufacturadoDetalles()).hasSize(1);
            assertThat(guardado.getPrecioCosto()).isEqualByComparingTo("800"); // 0.2 kg * 4000
            assertThat(guardado.getUnidadMedidaEnum()).isEqualTo(UnidadMedidaEnum.UNIDAD);
            assertThat(guardado.getPrecioVenta()).isEqualByComparingTo("5000");
            assertThat(guardado.getEsVendible()).isTrue();
            assertThat(guardado.getStockActual()).isNull();
        }

        @Test
        @DisplayName("Un plato final en una categoría de Insumos es rechazado")
        void platoFinalEnCategoriaDeInsumos_lanzaExcepcion() {
            when(categoriaRepository.findById(3L)).thenReturn(Optional.of(
                    Categoria.builder().denominacion("Lácteos").tipo(TipoCategoria.INSUMO).build()));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.crearManufacturado(platoFinalRequest(null)));

            assertThat(ex.getMessage()).contains("Lácteos").contains("Productos");
            verify(manufacturadoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Un semielaborado en una categoría de Productos es rechazado")
        void semielaboradoEnCategoriaDeProductos_lanzaExcepcion() {
            when(categoriaRepository.findById(4L)).thenReturn(Optional.of(
                    Categoria.builder().denominacion("Pizzas").tipo(TipoCategoria.PRODUCTO).build()));

            assertThrows(IllegalArgumentException.class,
                    () -> service.crearManufacturado(semielaboradoRequest(5.0, 1.0, 20.0)));
        }

        @Test
        @DisplayName("Semielaborado sin stock mínimo/máximo es rechazado")
        void semielaboradoSinStockMinMax_lanzaExcepcion() {
            when(categoriaRepository.findById(4L)).thenReturn(Optional.of(Categoria.builder().denominacion("Panificados").tipo(TipoCategoria.INSUMO).build()));

            assertThrows(IllegalArgumentException.class,
                    () -> service.crearManufacturado(semielaboradoRequest(5.0, null, 20.0)));
        }

        @Test
        @DisplayName("Semielaborado válido: arranca sin stock (sale de los lotes), no es vendible, precio 0, respeta la unidad")
        void semielaboradoValido_seArmaYGuarda() {
            when(categoriaRepository.findById(4L)).thenReturn(Optional.of(Categoria.builder().denominacion("Panificados").tipo(TipoCategoria.INSUMO).build()));

            service.crearManufacturado(semielaboradoRequest(5.0, 1.0, 20.0));

            ArgumentCaptor<ArticuloManufacturado> captor = ArgumentCaptor.forClass(ArticuloManufacturado.class);
            verify(manufacturadoRepository).save(captor.capture());
            ArticuloManufacturado guardado = captor.getValue();
            assertThat(guardado.getEsVendible()).isFalse();
            // El stockActual del request se ignora: el stock de un semielaborado es la suma de sus lotes.
            assertThat(guardado.getStockActual()).isEqualTo(0.0);
            assertThat(guardado.getPrecioVenta()).isEqualByComparingTo("0");
            assertThat(guardado.getStockMinimo()).isEqualTo(1.0);
            assertThat(guardado.getStockMaximo()).isEqualTo(20.0);
            assertThat(guardado.getUnidadMedidaEnum()).isEqualTo(UnidadMedidaEnum.KG);
        }

        @Test
        @DisplayName("Semielaborado sin stockActual en el request arranca en 0")
        void semielaboradoSinStockActual_arrancaEnCero() {
            when(categoriaRepository.findById(4L)).thenReturn(Optional.of(Categoria.builder().denominacion("Panificados").tipo(TipoCategoria.INSUMO).build()));

            service.crearManufacturado(semielaboradoRequest(null, 1.0, 20.0));

            ArgumentCaptor<ArticuloManufacturado> captor = ArgumentCaptor.forClass(ArticuloManufacturado.class);
            verify(manufacturadoRepository).save(captor.capture());
            assertThat(captor.getValue().getStockActual()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {
        @Test
        @DisplayName("Manufacturado inexistente lanza EntityNotFoundException")
        void noEncontrado_lanzaExcepcion() {
            when(manufacturadoRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> service.update(1L, platoFinalRequest(null)));
        }

        @Test
        @DisplayName("Con detalles null en el request, no toca la receta existente")
        void detallesNull_noTocaReceta() {
            ArticuloInsumo queso = insumoConCosto(4000, 10.0);
            ArticuloManufacturadoDetalle detalleExistente = ArticuloManufacturadoDetalle.builder()
                    .componente(queso).cantidad(0.2).build();
            ArticuloManufacturado existente = platoFinal("Pizza", detalleExistente);
            when(manufacturadoRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(categoriaRepository.findById(3L)).thenReturn(Optional.of(Categoria.builder().build()));

            service.update(1L, platoFinalRequest(null));

            assertThat(existente.getArticuloManufacturadoDetalles()).hasSize(1);
        }

        @Test
        @DisplayName("Con detalles en el request, reemplaza la receta completa")
        void detallesEnviados_reemplazaReceta() {
            ArticuloInsumo quesoViejo = insumoConCosto(4000, 10.0);
            ArticuloManufacturadoDetalle detalleViejo = ArticuloManufacturadoDetalle.builder()
                    .componente(quesoViejo).cantidad(0.2).build();
            ArticuloManufacturado existente = platoFinal("Pizza", detalleViejo);
            when(manufacturadoRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(categoriaRepository.findById(3L)).thenReturn(Optional.of(Categoria.builder().build()));

            ArticuloInsumo jamon = insumoConCosto(6000, 10.0);
            when(articuloRepository.findById(8L)).thenReturn(Optional.of(jamon));
            List<ArticuloManufacturadoDetalleRequest> nuevosDetalles = List.of(new ArticuloManufacturadoDetalleRequest(8L, 0.1));

            service.update(1L, platoFinalRequest(nuevosDetalles));

            assertThat(existente.getArticuloManufacturadoDetalles()).hasSize(1);
            assertThat(existente.getArticuloManufacturadoDetalles().get(0).getComponente()).isSameAs(jamon);
        }
    }

    @Nested
    @DisplayName("sincronizarImagenes (a través de crearManufacturado/update)")
    class SincronizarImagenes {
        @Test
        @DisplayName("URL no https es rechazada")
        void urlNoHttps_lanzaExcepcion() {
            when(categoriaRepository.findById(3L)).thenReturn(Optional.of(Categoria.builder().build()));
            ArticuloManufacturadoRequest request = new ArticuloManufacturadoRequest(
                    "Pizza", false, null, null, null, null, "desc", BigDecimal.valueOf(5000), null, "prep",
                    new CategoriaRequest("Pizzas", 3L),
                    List.of(new ImagenArticuloRequest("foto", "http://inseguro.com/a.jpg")), null);

            assertThrows(IllegalArgumentException.class, () -> service.crearManufacturado(request));
        }

        @Test
        @DisplayName("Con imagenes null, no toca las fotos existentes")
        void imagenesNull_noLasToca() {
            ArticuloManufacturado existente = platoFinal("Pizza");
            existente.getImagenes().add(ImagenArticulo.builder().denominacion("Pizza").urlImagen("https://res.cloudinary.com/x/1.jpg").articulo(existente).build());
            when(manufacturadoRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(categoriaRepository.findById(3L)).thenReturn(Optional.of(Categoria.builder().build()));

            service.update(1L, platoFinalRequest(null));

            assertThat(existente.getImagenes()).hasSize(1);
            verify(cloudinaryService, never()).eliminarImagen(any());
        }

        @Test
        @DisplayName("Agrega las nuevas y borra de Cloudinary solo las URLs realmente alojadas ahí")
        void diffAgregaYBorraSoloCloudinary() {
            ArticuloManufacturado existente = platoFinal("Pizza");
            existente.getImagenes().add(ImagenArticulo.builder().denominacion("Pizza").urlImagen("https://res.cloudinary.com/x/vieja.jpg").articulo(existente).build());
            existente.getImagenes().add(ImagenArticulo.builder().denominacion("Pizza").urlImagen("https://otrodominio.com/externa.jpg").articulo(existente).build());
            when(manufacturadoRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(categoriaRepository.findById(3L)).thenReturn(Optional.of(Categoria.builder().build()));

            ArticuloManufacturadoRequest request = new ArticuloManufacturadoRequest(
                    "Pizza", false, null, null, null, null, "desc", BigDecimal.valueOf(5000), null, "prep",
                    new CategoriaRequest("Pizzas", 3L),
                    List.of(new ImagenArticuloRequest("foto", "https://res.cloudinary.com/x/nueva.jpg")), null);

            service.update(1L, request);

            assertThat(existente.getImagenes()).extracting(ImagenArticulo::getUrlImagen)
                    .containsExactly("https://res.cloudinary.com/x/nueva.jpg");
            verify(cloudinaryService).eliminarImagen("https://res.cloudinary.com/x/vieja.jpg");
            verify(cloudinaryService, never()).eliminarImagen("https://otrodominio.com/externa.jpg");
        }

        @Test
        @DisplayName("No duplica una imagen que ya estaba guardada")
        void noDuplicaImagenExistente() {
            ArticuloManufacturado existente = platoFinal("Pizza");
            existente.getImagenes().add(ImagenArticulo.builder().denominacion("Pizza").urlImagen("https://res.cloudinary.com/x/a.jpg").articulo(existente).build());
            when(manufacturadoRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(categoriaRepository.findById(3L)).thenReturn(Optional.of(Categoria.builder().build()));

            ArticuloManufacturadoRequest request = new ArticuloManufacturadoRequest(
                    "Pizza", false, null, null, null, null, "desc", BigDecimal.valueOf(5000), null, "prep",
                    new CategoriaRequest("Pizzas", 3L),
                    List.of(new ImagenArticuloRequest("foto", "https://res.cloudinary.com/x/a.jpg")), null);

            service.update(1L, request);

            assertThat(existente.getImagenes()).hasSize(1);
            verify(cloudinaryService, never()).eliminarImagen(any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {
        @Test
        @DisplayName("Manufacturado inexistente lanza EntityNotFoundException")
        void noEncontrado_lanzaExcepcion() {
            when(manufacturadoRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> service.delete(1L));
            verify(manufacturadoRepository, never()).delete(any(ArticuloManufacturado.class));
        }

        @Test
        @DisplayName("Manufacturado existente se borra")
        void existente_seBorra() {
            ArticuloManufacturado plato = ArticuloManufacturado.builder().id(1L).nombre("Pizza").esSemielaborado(false).build();
            when(manufacturadoRepository.findById(1L)).thenReturn(Optional.of(plato));

            service.delete(1L);

            verify(manufacturadoRepository).delete(plato);
        }

        @Test
        @DisplayName("Un semielaborado con stock en lotes no se puede borrar")
        void semielaboradoConStock_noSeBorra() {
            ArticuloManufacturado masa = ArticuloManufacturado.builder().id(2L).nombre("Masa").esSemielaborado(true)
                    .stockReservado(0.0).existencias(new java.util.ArrayList<>()).build();
            masa.getExistencias().add(com.aluracursos.buensaborbp.entity.ExistenciaSemielaborado.builder()
                    .semielaborado(masa).fechaElaboracion(java.time.LocalDate.now())
                    .fechaVencimiento(java.time.LocalDate.now().plusDays(3)).cantidad(4.0).cantidadRestante(4.0).build());
            when(manufacturadoRepository.findById(2L)).thenReturn(Optional.of(masa));

            assertThrows(IllegalStateException.class, () -> service.delete(2L));
            verify(manufacturadoRepository, never()).delete(any(ArticuloManufacturado.class));
        }
    }
}
