package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.PromocionDetalleRequest;
import com.aluracursos.buensaborbp.dto.PromocionRequest;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturado;
import com.aluracursos.buensaborbp.entity.Articulo;
import com.aluracursos.buensaborbp.entity.Categoria;
import com.aluracursos.buensaborbp.entity.Enums.TipoPromocion;
import com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum;
import com.aluracursos.buensaborbp.entity.ImagenPromocion;
import com.aluracursos.buensaborbp.entity.Promocion;
import com.aluracursos.buensaborbp.entity.PromocionDetalle;
import com.aluracursos.buensaborbp.mapper.PromocionMapper;
import com.aluracursos.buensaborbp.repository.ArticuloRepository;
import com.aluracursos.buensaborbp.repository.CategoriaRepository;
import com.aluracursos.buensaborbp.repository.PromocionRepository;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// A diferencia de los otros dos services de artículos, acá crearPromocion/actualizarPromocion
// arman la entidad con el builder directamente (no vía mapper.toEntity), así que no hace falta
// stubear un mapeo manual. Se enfoca en lo que agrega esta capa sobre lo que ya prueba
// PromocionTest (cálculo de precios en la entidad): resolución de categoría/artículos por id,
// el default de unidad de medida, y sincronizarImagenes (mismo patrón que
// ArticuloManufacturadoServiceImpl, ver ArticuloManufacturadoServiceImplTest).
@ExtendWith(MockitoExtension.class)
class PromocionServiceImplTest {

    @Mock private PromocionRepository promocionRepository;
    @Mock private PromocionMapper promocionMapper;
    @Mock private ArticuloRepository articuloRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private CloudinaryService cloudinaryService;

    private PromocionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PromocionServiceImpl(promocionRepository, promocionMapper, articuloRepository, categoriaRepository, cloudinaryService);
        lenient().when(promocionRepository.save(any(Promocion.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static PromocionRequest requestCon(Long idCategoria, List<PromocionDetalleRequest> detalles,
                                                BigDecimal precioVenta, List<String> imagenesUrls) {
        return new PromocionRequest(
                "Combo Familiar", "20% off", LocalDate.now(), LocalDate.now().plusDays(30),
                LocalTime.of(11, 0), LocalTime.of(23, 0), precioVenta, TipoPromocion.HAPPY_HOUR,
                idCategoria, detalles, imagenesUrls);
    }

    private static ArticuloManufacturado productoFinal(double precioVenta, double precioCosto) {
        return ArticuloManufacturado.builder()
                .nombre("Hamburguesa").precioVenta(BigDecimal.valueOf(precioVenta)).precioCosto(BigDecimal.valueOf(precioCosto))
                .unidadMedidaEnum(UnidadMedidaEnum.UNIDAD).descripcion("d").preparacion("p").esSemielaborado(false)
                .build();
    }

    private static Promocion promocionExistente(Articulo... items) {
        Promocion promo = Promocion.builder()
                .nombre("Vieja").unidadMedidaEnum(UnidadMedidaEnum.UNIDAD)
                .precioVenta(BigDecimal.valueOf(1000))
                .promocionDetalle(new java.util.ArrayList<>())
                .build();
        for (Articulo item : items) {
            promo.getPromocionDetalle().add(PromocionDetalle.builder().articulos(item).cantidad(1).articuloPromocion(promo).build());
        }
        return promo;
    }

    @Nested
    @DisplayName("crearPromocion")
    class CrearPromocion {
        @Test
        @DisplayName("Categoría inexistente lanza EntityNotFoundException")
        void categoriaInexistente_lanzaExcepcion() {
            when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());
            List<PromocionDetalleRequest> detalles = List.of(new PromocionDetalleRequest(5L, 1));

            assertThrows(EntityNotFoundException.class,
                    () -> service.crearPromocion(requestCon(1L, detalles, BigDecimal.valueOf(6000), null)));
        }

        @Test
        @DisplayName("Una promoción en una categoría de Insumos es rechazada")
        void enCategoriaDeInsumos_lanzaExcepcion() {
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(Categoria.builder()
                    .denominacion("Lácteos").tipo(com.aluracursos.buensaborbp.entity.Enums.TipoCategoria.INSUMO).build()));
            List<PromocionDetalleRequest> detalles = List.of(new PromocionDetalleRequest(5L, 1));

            assertThrows(IllegalArgumentException.class,
                    () -> service.crearPromocion(requestCon(1L, detalles, BigDecimal.valueOf(6000), null)));
            verify(promocionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Artículo del detalle inexistente lanza EntityNotFoundException")
        void articuloInexistente_lanzaExcepcion() {
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(Categoria.builder().build()));
            when(articuloRepository.findById(5L)).thenReturn(Optional.empty());
            List<PromocionDetalleRequest> detalles = List.of(new PromocionDetalleRequest(5L, 1));

            assertThrows(EntityNotFoundException.class,
                    () -> service.crearPromocion(requestCon(1L, detalles, BigDecimal.valueOf(6000), null)));
        }

        @Test
        @DisplayName("Combo válido: arma los detalles, usa unidad UNIDAD y respeta el precio manual")
        void comboValido_seArmaYGuarda() {
            Categoria categoria = Categoria.builder().denominacion("Combos").build();
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
            ArticuloManufacturado hamburguesa = productoFinal(8000, 3000);
            when(articuloRepository.findById(5L)).thenReturn(Optional.of(hamburguesa));
            List<PromocionDetalleRequest> detalles = List.of(new PromocionDetalleRequest(5L, 2));

            service.crearPromocion(requestCon(1L, detalles, BigDecimal.valueOf(14000), null));

            ArgumentCaptor<Promocion> captor = ArgumentCaptor.forClass(Promocion.class);
            verify(promocionRepository).save(captor.capture());
            Promocion guardada = captor.getValue();
            assertThat(guardada.getCategoria()).isSameAs(categoria);
            assertThat(guardada.getUnidadMedidaEnum()).isEqualTo(UnidadMedidaEnum.UNIDAD);
            assertThat(guardada.getPromocionDetalle()).hasSize(1);
            assertThat(guardada.getPrecioCosto()).isEqualByComparingTo("6000"); // 2 * 3000
            assertThat(guardada.getPrecioTotal()).isEqualByComparingTo("16000"); // 2 * 8000
            assertThat(guardada.getPrecioVenta()).isEqualByComparingTo("14000"); // precio manual, no el de lista
        }

        @Test
        @DisplayName("Sin precio de venta manual, usa el precio de lista de los componentes")
        void sinPrecioManual_usaPrecioDeLista() {
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(Categoria.builder().build()));
            ArticuloManufacturado hamburguesa = productoFinal(8000, 3000);
            when(articuloRepository.findById(5L)).thenReturn(Optional.of(hamburguesa));
            List<PromocionDetalleRequest> detalles = List.of(new PromocionDetalleRequest(5L, 1));

            service.crearPromocion(requestCon(1L, detalles, null, null));

            ArgumentCaptor<Promocion> captor = ArgumentCaptor.forClass(Promocion.class);
            verify(promocionRepository).save(captor.capture());
            assertThat(captor.getValue().getPrecioVenta()).isEqualByComparingTo("8000");
        }
    }

    @Nested
    @DisplayName("actualizarPromocion")
    class ActualizarPromocion {
        @Test
        @DisplayName("Promoción inexistente lanza EntityNotFoundException")
        void noEncontrada_lanzaExcepcion() {
            when(promocionRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.actualizarPromocion(1L, requestCon(1L, List.of(), BigDecimal.TEN, null)));
        }

        @Test
        @DisplayName("Categoría inexistente lanza EntityNotFoundException")
        void categoriaInexistente_lanzaExcepcion() {
            ArticuloManufacturado item = productoFinal(8000, 3000);
            Promocion existente = promocionExistente(item);
            when(promocionRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(categoriaRepository.findById(2L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.actualizarPromocion(1L, requestCon(2L, List.of(), BigDecimal.TEN, null)));
        }

        @Test
        @DisplayName("Reemplaza los detalles existentes por los nuevos del request")
        void reemplazaDetalles() {
            ArticuloManufacturado itemViejo = productoFinal(8000, 3000);
            Promocion existente = promocionExistente(itemViejo);
            when(promocionRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(categoriaRepository.findById(2L)).thenReturn(Optional.of(Categoria.builder().build()));
            ArticuloManufacturado itemNuevo = productoFinal(9000, 4000);
            when(articuloRepository.findById(6L)).thenReturn(Optional.of(itemNuevo));
            List<PromocionDetalleRequest> nuevosDetalles = List.of(new PromocionDetalleRequest(6L, 1));

            service.actualizarPromocion(1L, requestCon(2L, nuevosDetalles, BigDecimal.valueOf(9000), null));

            assertThat(existente.getPromocionDetalle()).hasSize(1);
            assertThat(existente.getPromocionDetalle().get(0).getArticulos()).isSameAs(itemNuevo);
        }
    }

    @Nested
    @DisplayName("sincronizarImagenes (a través de crearPromocion/actualizarPromocion)")
    class SincronizarImagenes {
        @Test
        @DisplayName("URL no https es rechazada")
        void urlNoHttps_lanzaExcepcion() {
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(Categoria.builder().build()));
            ArticuloManufacturado item = productoFinal(8000, 3000);
            when(articuloRepository.findById(5L)).thenReturn(Optional.of(item));
            List<PromocionDetalleRequest> detalles = List.of(new PromocionDetalleRequest(5L, 1));

            assertThrows(IllegalArgumentException.class, () -> service.crearPromocion(
                    requestCon(1L, detalles, BigDecimal.TEN, List.of("http://inseguro.com/a.jpg"))));
        }

        @Test
        @DisplayName("Con imagenesUrls null, no toca las fotos existentes")
        void imagenesNull_noLasToca() {
            ArticuloManufacturado item = productoFinal(8000, 3000);
            Promocion existente = promocionExistente(item);
            existente.getImagenesPromocion().add(ImagenPromocion.builder()
                    .denominacion("Vieja").urlImagen("https://res.cloudinary.com/x/1.jpg").promocion(existente).build());
            when(promocionRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(categoriaRepository.findById(2L)).thenReturn(Optional.of(Categoria.builder().build()));
            when(articuloRepository.findById(5L)).thenReturn(Optional.of(item));
            List<PromocionDetalleRequest> detalles = List.of(new PromocionDetalleRequest(5L, 1));

            service.actualizarPromocion(1L, requestCon(2L, detalles, BigDecimal.TEN, null));

            assertThat(existente.getImagenesPromocion()).hasSize(1);
            verify(cloudinaryService, never()).eliminarImagen(any());
        }

        @Test
        @DisplayName("Agrega las nuevas y borra de Cloudinary solo las URLs realmente alojadas ahí")
        void diffAgregaYBorraSoloCloudinary() {
            ArticuloManufacturado item = productoFinal(8000, 3000);
            Promocion existente = promocionExistente(item);
            existente.getImagenesPromocion().add(ImagenPromocion.builder()
                    .denominacion("Vieja").urlImagen("https://res.cloudinary.com/x/vieja.jpg").promocion(existente).build());
            existente.getImagenesPromocion().add(ImagenPromocion.builder()
                    .denominacion("Externa").urlImagen("https://otrodominio.com/externa.jpg").promocion(existente).build());
            when(promocionRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(categoriaRepository.findById(2L)).thenReturn(Optional.of(Categoria.builder().build()));
            when(articuloRepository.findById(5L)).thenReturn(Optional.of(item));
            List<PromocionDetalleRequest> detalles = List.of(new PromocionDetalleRequest(5L, 1));

            service.actualizarPromocion(1L, requestCon(2L, detalles, BigDecimal.TEN,
                    List.of("https://res.cloudinary.com/x/nueva.jpg")));

            assertThat(existente.getImagenesPromocion()).extracting(ImagenPromocion::getUrlImagen)
                    .containsExactly("https://res.cloudinary.com/x/nueva.jpg");
            verify(cloudinaryService).eliminarImagen("https://res.cloudinary.com/x/vieja.jpg");
            verify(cloudinaryService, never()).eliminarImagen("https://otrodominio.com/externa.jpg");
        }
    }

    @Nested
    @DisplayName("cambiarEstadoHabilitacion")
    class CambiarEstadoHabilitacion {
        @Test
        @DisplayName("Promoción inexistente lanza EntityNotFoundException")
        void noEncontrada_lanzaExcepcion() {
            when(promocionRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> service.cambiarEstadoHabilitacion(1L));
        }

        @Test
        @DisplayName("Alterna de activa a inactiva")
        void activaAInactiva() {
            Promocion promo = promocionExistente();
            promo.setActivo(true);
            when(promocionRepository.findById(1L)).thenReturn(Optional.of(promo));

            service.cambiarEstadoHabilitacion(1L);

            assertThat(promo.getActivo()).isFalse();
        }

        @Test
        @DisplayName("Alterna de inactiva a activa")
        void inactivaAActiva() {
            Promocion promo = promocionExistente();
            promo.setActivo(false);
            when(promocionRepository.findById(1L)).thenReturn(Optional.of(promo));

            service.cambiarEstadoHabilitacion(1L);

            assertThat(promo.getActivo()).isTrue();
        }
    }
}
