package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.ArticuloInsumoRequest;
import com.aluracursos.buensaborbp.dto.CategoriaRequest;
import com.aluracursos.buensaborbp.dto.UnidadMedidaRequest;
import com.aluracursos.buensaborbp.entity.ArticuloInsumo;
import com.aluracursos.buensaborbp.entity.Categoria;
import com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum;
import com.aluracursos.buensaborbp.mapper.ArticuloInsumoMapper;
import com.aluracursos.buensaborbp.repository.ArticuloInsumoRepository;
import com.aluracursos.buensaborbp.repository.CategoriaRepository;
import com.aluracursos.buensaborbp.repository.ArticuloManufacturadoDetalleRepository;
import com.aluracursos.buensaborbp.repository.PromocionRepository;
import com.aluracursos.buensaborbp.dto.UsoInsumoResponse;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturado;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturadoDetalle;
import com.aluracursos.buensaborbp.entity.Promocion;
import java.util.List;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

// Cubre la capa de servicio de ArticuloInsumo: los "puentes" de stock que llama PedidoServiceImpl
// (reservar/liberar/confirmar) y el alta/edición, que fue el foco del bloque 2 de bugs de esta
// sesión (margen/precio de venta, categoría obligatoria). Como el mapper es una interfaz MapStruct
// generada en compilación, acá se mockea y se stubea `toEntity` replicando su mapeo real
// (ver ArticuloInsumoMapper: nombre<-denominacion, precioCosto<-precioCompra, y el resto
// auto-mapeado por coincidencia de nombre) para que el service tenga una entidad real con la que
// trabajar, igual que en producción.
@ExtendWith(MockitoExtension.class)
class ArticuloInsumoServiceImplTest {

    @Mock private ArticuloInsumoRepository articuloInsumoRepository;
    @Mock private ArticuloInsumoMapper articuloInsumoMapper;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private ArticuloManufacturadoDetalleRepository detalleRepository;
    @Mock private PromocionRepository promocionRepository;
    @Mock private RecalculoCostoProductos recalculoCostoProductos;
    @Mock private ImagenesArticulo imagenesArticulo;

    private ArticuloInsumoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ArticuloInsumoServiceImpl(articuloInsumoRepository, articuloInsumoMapper, categoriaRepository,
                detalleRepository, promocionRepository, recalculoCostoProductos, imagenesArticulo);
        lenient().when(articuloInsumoRepository.save(any(ArticuloInsumo.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(articuloInsumoMapper.toEntity(any(ArticuloInsumoRequest.class)))
                .thenAnswer(inv -> mapearComoLoHaceElMapperReal(inv.getArgument(0)));
    }

    private static ArticuloInsumo mapearComoLoHaceElMapperReal(ArticuloInsumoRequest r) {
        return ArticuloInsumo.builder()
                .nombre(r.denominacion())
                .precioCosto(r.precioCompra())
                .precioVenta(r.precioVenta())
                .margen(r.margen())
                .stockActual(r.stockActual())
                .stockMinimo(r.stockMinimo())
                .stockMaximo(r.stockMaximo())
                .esVendible(!Boolean.TRUE.equals(r.esParaElaborar()))
                .unidadMedidaEnum(r.unidadMedida() == null ? null
                        : UnidadMedidaEnum.desdeCatalogo(r.unidadMedida().denominacion(), r.unidadMedida().abreviatura()))
                .build();
    }

    private static ArticuloInsumoRequest requestValido() {
        return requestConMargen(null);
    }

    private static ArticuloInsumoRequest requestConMargen(Float margen) {
        return new ArticuloInsumoRequest(
                "Coca 500ml", BigDecimal.valueOf(100), BigDecimal.valueOf(150), margen,
                10.0, 2.0, 50.0, false, null,
                new UnidadMedidaRequest("Unidad", "un"),
                new CategoriaRequest("Bebidas", 5L));
    }

    private static ArticuloInsumo insumoExistente() {
        return ArticuloInsumo.builder()
                .nombre("Coca 500ml").precioVenta(BigDecimal.TEN).precioCosto(BigDecimal.TEN)
                .unidadMedidaEnum(UnidadMedidaEnum.UNIDAD)
                .stockActual(10.0).stockMaximo(50.0).stockMinimo(2.0).stockReservado(0.0)
                .build();
    }

    @Nested
    @DisplayName("reservarStock")
    class ReservarStock {
        @Test
        @DisplayName("Delega en la entidad y persiste el cambio")
        void delegaYPersiste() {
            ArticuloInsumo insumo = insumoExistente();
            when(articuloInsumoRepository.findById(1L)).thenReturn(Optional.of(insumo));

            service.reservarStock(1L, 3, "Pizza Muzzarella");

            assertThat(insumo.getStockReservado()).isEqualTo(3.0);
            verify(articuloInsumoRepository).save(insumo);
        }

        @Test
        @DisplayName("Insumo inexistente lanza EntityNotFoundException")
        void noEncontrado_lanzaExcepcion() {
            when(articuloInsumoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> service.reservarStock(99L, 1, "X"));
        }
    }

    @Nested
    @DisplayName("liberarStock")
    class LiberarStock {
        @Test
        @DisplayName("Delega en la entidad y persiste el cambio")
        void delegaYPersiste() {
            ArticuloInsumo insumo = insumoExistente();
            insumo.setStockReservado(5.0);
            when(articuloInsumoRepository.findById(1L)).thenReturn(Optional.of(insumo));

            service.liberarStock(1L, 2);

            assertThat(insumo.getStockReservado()).isEqualTo(3.0);
            verify(articuloInsumoRepository).save(insumo);
        }

        @Test
        @DisplayName("Insumo inexistente lanza EntityNotFoundException")
        void noEncontrado_lanzaExcepcion() {
            when(articuloInsumoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> service.liberarStock(99L, 1));
        }
    }

    @Nested
    @DisplayName("confirmarVenta")
    class ConfirmarVenta {
        @Test
        @DisplayName("Delega en la entidad y persiste el cambio")
        void delegaYPersiste() {
            ArticuloInsumo insumo = insumoExistente();
            insumo.setStockReservado(4.0);
            when(articuloInsumoRepository.findById(1L)).thenReturn(Optional.of(insumo));

            service.confirmarVenta(1L, 4);

            assertThat(insumo.getStockActual()).isEqualTo(6.0);
            assertThat(insumo.getStockReservado()).isEqualTo(0.0);
            verify(articuloInsumoRepository).save(insumo);
        }

        @Test
        @DisplayName("Insumo inexistente lanza EntityNotFoundException")
        void noEncontrado_lanzaExcepcion() {
            when(articuloInsumoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> service.confirmarVenta(99L, 1));
        }
    }

    @Nested
    @DisplayName("crearArticuloInsumo")
    class CrearArticuloInsumo {
        @Test
        @DisplayName("Sin categoría, rechaza con IllegalArgumentException")
        void sinCategoria_lanzaExcepcion() {
            ArticuloInsumoRequest request = new ArticuloInsumoRequest(
                    "Coca", BigDecimal.TEN, BigDecimal.valueOf(20), null, 1.0, 1.0, 10.0, false,
                    null, new UnidadMedidaRequest("Unidad", "un"), null);

            assertThrows(IllegalArgumentException.class, () -> service.crearArticuloInsumo(request));
        }

        @Test
        @DisplayName("Sin unidad de medida, rechaza con IllegalArgumentException")
        void sinUnidadMedida_lanzaExcepcion() {
            ArticuloInsumoRequest request = new ArticuloInsumoRequest(
                    "Coca", BigDecimal.TEN, BigDecimal.valueOf(20), null, 1.0, 1.0, 10.0, false,
                    null, null, new CategoriaRequest("Bebidas", 5L));

            assertThrows(IllegalArgumentException.class, () -> service.crearArticuloInsumo(request));
        }

        @Test
        @DisplayName("Categoría inexistente lanza EntityNotFoundException")
        void categoriaInexistente_lanzaExcepcion() {
            when(categoriaRepository.findById(5L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> service.crearArticuloInsumo(requestValido()));
        }

        @Test
        @DisplayName("Con margen, calcula el precio de venta a partir del costo en vez de usar el manual")
        void conMargen_calculaPrecioPorMargen() {
            Categoria categoria = Categoria.builder().denominacion("Bebidas").build();
            when(categoriaRepository.findById(5L)).thenReturn(Optional.of(categoria));

            service.crearArticuloInsumo(requestConMargen(50f));

            ArgumentCaptor<ArticuloInsumo> captor = ArgumentCaptor.forClass(ArticuloInsumo.class);
            verify(articuloInsumoRepository).save(captor.capture());
            ArticuloInsumo guardado = captor.getValue();
            assertThat(guardado.getCategoria()).isSameAs(categoria);
            assertThat(guardado.getPrecioVenta()).isEqualByComparingTo("150"); // 100 * 1.5
        }

        @Test
        @DisplayName("Sin margen, usa el precio de venta manual tal cual")
        void sinMargen_usaPrecioManual() {
            when(categoriaRepository.findById(5L)).thenReturn(Optional.of(Categoria.builder().build()));

            service.crearArticuloInsumo(requestValido());

            ArgumentCaptor<ArticuloInsumo> captor = ArgumentCaptor.forClass(ArticuloInsumo.class);
            verify(articuloInsumoRepository).save(captor.capture());
            assertThat(captor.getValue().getPrecioVenta()).isEqualByComparingTo("150");
        }

        @Test
        @DisplayName("El stock ingresado se interpreta en la unidad elegida y se guarda en la unidad base")
        void convierteStockSegunLaUnidadElegida() {
            when(categoriaRepository.findById(5L)).thenReturn(Optional.of(Categoria.builder().build()));
            // 1850 gramos de stock inicial.
            ArticuloInsumoRequest conGramos = new ArticuloInsumoRequest(
                    "Queso rallado", BigDecimal.TEN, BigDecimal.valueOf(20), null, 1850.0, 100.0, 5000.0, false,
                    null, new UnidadMedidaRequest("Gramos", "gr"), new CategoriaRequest("Bebidas", 5L));

            service.crearArticuloInsumo(conGramos);

            ArgumentCaptor<ArticuloInsumo> captor = ArgumentCaptor.forClass(ArticuloInsumo.class);
            verify(articuloInsumoRepository).save(captor.capture());
            assertThat(captor.getValue().getStockActual()).isEqualTo(1.85); // 1850 gr = 1.85 kg (base)
        }
    }

    @Nested
    @DisplayName("update")
    class Update {
        @Test
        @DisplayName("Insumo inexistente lanza EntityNotFoundException")
        void noEncontrado_lanzaExcepcion() {
            when(articuloInsumoRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> service.update(1L, requestValido()));
        }

        @Test
        @DisplayName("Actualiza el stockActual/Minimo/Maximo con los valores del request")
        void actualizaStock() {
            ArticuloInsumo existente = insumoExistente(); // unidad UNIDAD, factor 1 => sin conversión
            existente.setStockActual(37.0);
            when(articuloInsumoRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(categoriaRepository.findById(5L)).thenReturn(Optional.of(Categoria.builder().build()));

            service.update(1L, requestValido()); // stockActual=10, stockMinimo=2, stockMaximo=50

            assertThat(existente.getStockActual()).isEqualTo(10.0);
            assertThat(existente.getStockMinimo()).isEqualTo(2.0);
            assertThat(existente.getStockMaximo()).isEqualTo(50.0);
        }

        @Test
        @DisplayName("Actualiza la unidad de medida cuando el request la trae")
        void actualizaUnidadDeMedida() {
            ArticuloInsumo existente = insumoExistente(); // arranca en UNIDAD
            when(articuloInsumoRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(categoriaRepository.findById(5L)).thenReturn(Optional.of(Categoria.builder().build()));
            ArticuloInsumoRequest conKilos = new ArticuloInsumoRequest(
                    "Coca", BigDecimal.TEN, BigDecimal.valueOf(20), null, 5.0, 1.0, 10.0, false,
                    null, new UnidadMedidaRequest("Kilos", "kg"), new CategoriaRequest("Bebidas", 5L));

            service.update(1L, conKilos);

            assertThat(existente.getUnidadMedidaEnum()).isEqualTo(UnidadMedidaEnum.KG);
        }

        @Test
        @DisplayName("El stock del request se interpreta en la unidad que trae el request (ej. Gramos) y se guarda en la unidad base")
        void convierteStockSegunLaUnidadDelRequest() {
            ArticuloInsumo existente = insumoExistente(); // arranca en UNIDAD, stockActual 10
            when(articuloInsumoRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(categoriaRepository.findById(5L)).thenReturn(Optional.of(Categoria.builder().build()));
            // El admin cambió la unidad a Gramos y tipeó 1850 (gramos) como stock actual.
            ArticuloInsumoRequest conGramos = new ArticuloInsumoRequest(
                    "Coca", BigDecimal.TEN, BigDecimal.valueOf(20), null, 1850.0, 100.0, 5000.0, false,
                    null, new UnidadMedidaRequest("Gramos", "gr"), new CategoriaRequest("Bebidas", 5L));

            service.update(1L, conGramos);

            assertThat(existente.getUnidadMedidaEnum()).isEqualTo(UnidadMedidaEnum.GR);
            assertThat(existente.getStockActual()).isEqualTo(1.85); // 1850 gr = 1.85 kg (unidad base)
            assertThat(existente.getStockMinimo()).isEqualTo(0.1);
            assertThat(existente.getStockMaximo()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("Actualiza la categoría cuando el request la trae")
        void actualizaCategoriaSiViene() {
            ArticuloInsumo existente = insumoExistente();
            Categoria nueva = Categoria.builder().denominacion("Nueva").build();
            when(articuloInsumoRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(categoriaRepository.findById(5L)).thenReturn(Optional.of(nueva));

            service.update(1L, requestValido());

            assertThat(existente.getCategoria()).isSameAs(nueva);
        }

        @Test
        @DisplayName("Sin categoría en el request, no la toca")
        void noTocaCategoriaSiNoViene() {
            ArticuloInsumo existente = insumoExistente();
            Categoria original = Categoria.builder().denominacion("Original").build();
            existente.setCategoria(original);
            when(articuloInsumoRepository.findById(1L)).thenReturn(Optional.of(existente));

            ArticuloInsumoRequest sinCategoria = new ArticuloInsumoRequest(
                    "Coca", BigDecimal.TEN, BigDecimal.valueOf(20), null, 1.0, 1.0, 10.0, false,
                    null, new UnidadMedidaRequest("Unidad", "un"), null);

            service.update(1L, sinCategoria);

            assertThat(existente.getCategoria()).isSameAs(original);
        }
    }

    // Cambiar la unidad de un insumo convierte las cantidades de las recetas que lo usan, para
    // que sigan representando lo mismo (si no, "250" gramos pasaría a leerse como 250 kilos).
    @Nested
    @DisplayName("update: cambio de unidad con recetas que usan el insumo")
    class CambioDeUnidadConRecetas {

        private ArticuloInsumo carneEnGramos() {
            return ArticuloInsumo.builder()
                    .id(17L).nombre("Carne molida").precioVenta(BigDecimal.TEN).precioCosto(BigDecimal.TEN)
                    .unidadMedidaEnum(UnidadMedidaEnum.GR)
                    .stockActual(7.0).stockMaximo(10.0).stockMinimo(5.0).stockReservado(0.0)
                    .build();
        }

        private ArticuloInsumoRequest pasarA(String denominacion, String abreviatura, double stockActual) {
            return new ArticuloInsumoRequest(
                    "Carne molida", BigDecimal.TEN, BigDecimal.TEN, null, stockActual, 5.0, 10.0, true,
                    null, new UnidadMedidaRequest(denominacion, abreviatura), null);
        }

        private ArticuloManufacturadoDetalle receta(String producto, double cantidad, ArticuloInsumo insumo) {
            return ArticuloManufacturadoDetalle.builder()
                    .cantidad(cantidad).componente(insumo)
                    .articuloManufacturado(ArticuloManufacturado.builder().id(1L).nombre(producto).build())
                    .build();
        }

        @Test
        @DisplayName("De Gramos a Kilos, convierte las cantidades de todas las recetas (250 -> 0.25)")
        void gramosAKilos_convierteRecetas() {
            ArticuloInsumo carne = carneEnGramos();
            ArticuloManufacturadoDetalle hamburguesa = receta("Hamburguesa Cheese", 250, carne);
            ArticuloManufacturadoDetalle mexicana = receta("Mexicana TX", 200, carne);
            when(articuloInsumoRepository.findById(17L)).thenReturn(Optional.of(carne));
            when(detalleRepository.findByComponente_Id(17L)).thenReturn(List.of(hamburguesa, mexicana));

            service.update(17L, pasarA("Kilos", "kg", 7.0));

            assertThat(hamburguesa.getCantidad()).isEqualTo(0.25);
            assertThat(mexicana.getCantidad()).isEqualTo(0.2);
            verify(detalleRepository).save(hamburguesa);
            verify(detalleRepository).save(mexicana);
        }

        @Test
        @DisplayName("De Kilos a Gramos, multiplica (0.15 -> 150)")
        void kilosAGramos_convierteRecetas() {
            ArticuloInsumo carne = carneEnGramos();
            carne.setUnidadMedidaEnum(UnidadMedidaEnum.KG);
            ArticuloManufacturadoDetalle pizza = receta("Pizza", 0.15, carne);
            when(articuloInsumoRepository.findById(17L)).thenReturn(Optional.of(carne));
            when(detalleRepository.findByComponente_Id(17L)).thenReturn(List.of(pizza));

            service.update(17L, pasarA("Gramos", "gr", 7000.0));

            assertThat(pizza.getCantidad()).isEqualTo(150.0);
        }

        @Test
        @DisplayName("Después de guardar, pide recalcular el costo de los productos que usan el insumo")
        void alGuardar_recalculaCostoDeProductos() {
            when(articuloInsumoRepository.findById(17L)).thenReturn(Optional.of(carneEnGramos()));

            service.update(17L, pasarA("Gramos", "gr", 7000.0));

            verify(recalculoCostoProductos).recalcularProductosQueUsan(17L);
        }

        @Test
        @DisplayName("Si la unidad no cambia, no toca ninguna receta")
        void mismaUnidad_noTocaRecetas() {
            when(articuloInsumoRepository.findById(17L)).thenReturn(Optional.of(carneEnGramos()));

            service.update(17L, pasarA("Gramos", "gr", 7000.0));

            verifyNoInteractions(detalleRepository);
        }

        @Test
        @DisplayName("Si el insumo está directo en una promoción, rechaza el cambio y no toca nada")
        void enPromocion_rechazaElCambio() {
            ArticuloInsumo carne = carneEnGramos();
            when(articuloInsumoRepository.findById(17L)).thenReturn(Optional.of(carne));
            when(promocionRepository.findQueIncluyenArticulo(17L))
                    .thenReturn(List.of(Promocion.builder().nombre("Combo Asado").build()));

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> service.update(17L, pasarA("Kilos", "kg", 7.0)));

            assertThat(ex.getMessage()).contains("Combo Asado");
            assertThat(carne.getUnidadMedidaEnum()).isEqualTo(UnidadMedidaEnum.GR);
            verify(articuloInsumoRepository, never()).save(any());
            verifyNoInteractions(detalleRepository);
        }

        @Test
        @DisplayName("obtenerUso lista las recetas (con su cantidad actual) y las promociones")
        void obtenerUso_listaRecetasYPromociones() {
            ArticuloInsumo carne = carneEnGramos();
            when(articuloInsumoRepository.existsById(17L)).thenReturn(true);
            when(detalleRepository.findByComponente_Id(17L)).thenReturn(List.of(receta("Hamburguesa Cheese", 250, carne)));
            when(promocionRepository.findQueIncluyenArticulo(17L)).thenReturn(List.of());

            UsoInsumoResponse uso = service.obtenerUso(17L);

            assertThat(uso.recetas()).extracting(UsoInsumoResponse.RecetaQueLoUsa::producto, UsoInsumoResponse.RecetaQueLoUsa::cantidad)
                    .containsExactly(org.assertj.core.groups.Tuple.tuple("Hamburguesa Cheese", 250.0));
            assertThat(uso.promociones()).isEmpty();
        }
    }

    // Insumos que se venden directo en el menú (ej. una gaseosa): "¿Es para elaborar?" destildado.
    @Nested
    @DisplayName("Venta directa en el menú")
    class VentaDirecta {
        private ArticuloInsumoRequest gaseosa(boolean esParaElaborar, List<com.aluracursos.buensaborbp.dto.ImagenArticuloRequest> imagenes) {
            return new ArticuloInsumoRequest(
                    "Coca 500ml", BigDecimal.valueOf(800), BigDecimal.valueOf(1500), null,
                    24.0, 6.0, 48.0, esParaElaborar, imagenes,
                    new UnidadMedidaRequest("Unidad", "un"), new CategoriaRequest("Bebidas", 3L));
        }

        @Test
        @DisplayName("Al editar, destildar \"¿Es para elaborar?\" lo vuelve vendible (antes se ignoraba)")
        void update_aplicaEsParaElaborar() {
            ArticuloInsumo existente = insumoExistente();
            existente.setEsVendible(false);
            when(articuloInsumoRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(categoriaRepository.findById(3L)).thenReturn(Optional.of(Categoria.builder().build()));

            service.update(1L, gaseosa(false, null));

            assertThat(existente.getEsVendible()).isTrue();
        }

        @Test
        @DisplayName("Un insumo para elaborar en una categoría de Productos es rechazado, sin tocar fotos")
        void paraElaborarEnCategoriaDeProductos_lanzaExcepcion() {
            when(categoriaRepository.findById(3L)).thenReturn(Optional.of(
                    Categoria.builder().denominacion("Bebidas").tipo(com.aluracursos.buensaborbp.entity.Enums.TipoCategoria.PRODUCTO).build()));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.crearArticuloInsumo(gaseosa(true, List.of())));

            assertThat(ex.getMessage()).contains("Bebidas").contains("Insumos");
            verifyNoInteractions(imagenesArticulo);
            verify(articuloInsumoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Al editar, destildar el tilde con la categoría de Insumos que ya tenía es rechazado")
        void ventaDirectaEnCategoriaDeInsumos_lanzaExcepcion() {
            ArticuloInsumo existente = insumoExistente();
            existente.setCategoria(Categoria.builder().denominacion("Lácteos")
                    .tipo(com.aluracursos.buensaborbp.entity.Enums.TipoCategoria.INSUMO).build());
            when(articuloInsumoRepository.findById(1L)).thenReturn(Optional.of(existente));
            ArticuloInsumoRequest sinCategoria = new ArticuloInsumoRequest(
                    "Queso", BigDecimal.TEN, BigDecimal.valueOf(20), null, 1.0, 1.0, 10.0, false,
                    null, new UnidadMedidaRequest("Unidad", "un"), null);

            assertThrows(IllegalArgumentException.class, () -> service.update(1L, sinCategoria));
            verifyNoInteractions(imagenesArticulo);
        }

        @Test
        @DisplayName("Crear y editar sincronizan la foto del insumo")
        void crearYEditar_sincronizanFoto() {
            List<com.aluracursos.buensaborbp.dto.ImagenArticuloRequest> foto =
                    List.of(new com.aluracursos.buensaborbp.dto.ImagenArticuloRequest("Coca", "https://res.cloudinary.com/x/coca.jpg"));
            when(categoriaRepository.findById(3L)).thenReturn(Optional.of(Categoria.builder().build()));

            service.crearArticuloInsumo(gaseosa(false, foto));

            verify(imagenesArticulo).sincronizar(any(ArticuloInsumo.class), eq(foto));
        }

        @Test
        @DisplayName("getVendibles devuelve solo los activos que se venden directo, con la vista del menú")
        void getVendibles_usaLaVistaDelMenu() {
            ArticuloInsumo coca = insumoExistente();
            when(articuloInsumoRepository.findByActivoTrueAndEsVendibleTrue()).thenReturn(List.of(coca));
            com.aluracursos.buensaborbp.dto.InsumoMenuResponse vista =
                    new com.aluracursos.buensaborbp.dto.InsumoMenuResponse(1L, "Coca 500ml", BigDecimal.valueOf(1500), null, null);
            when(articuloInsumoMapper.toMenuResponse(coca)).thenReturn(vista);

            assertThat(service.getVendibles()).containsExactly(vista);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {
        @Test
        @DisplayName("Insumo inexistente lanza EntityNotFoundException")
        void noEncontrado_lanzaExcepcion() {
            when(articuloInsumoRepository.existsById(1L)).thenReturn(false);

            assertThrows(EntityNotFoundException.class, () -> service.delete(1L));
            verify(articuloInsumoRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("Insumo existente se borra")
        void existente_seBorra() {
            when(articuloInsumoRepository.existsById(1L)).thenReturn(true);

            service.delete(1L);

            verify(articuloInsumoRepository).deleteById(1L);
        }
    }
}
