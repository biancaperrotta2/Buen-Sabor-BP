package com.aluracursos.buensaborbp.scratch;

import com.aluracursos.buensaborbp.entity.ArticuloInsumo;
import com.aluracursos.buensaborbp.entity.ArticuloManufacturado;
import com.aluracursos.buensaborbp.entity.Categoria;
import com.aluracursos.buensaborbp.entity.UnidadMedida;
import com.aluracursos.buensaborbp.entity.Enums.UnidadMedidaEnum;
import com.aluracursos.buensaborbp.repository.ArticuloInsumoRepository;
import com.aluracursos.buensaborbp.repository.ArticuloManufacturadoRepository;
import com.aluracursos.buensaborbp.repository.CategoriaRepository;
import com.aluracursos.buensaborbp.repository.UnidadMedidaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.ArrayList;

@Configuration
@Profile("h2")
public class TestDataSeeder {

    @Bean
    public CommandLineRunner seedCatalogo(
            CategoriaRepository categoriaRepository,
            UnidadMedidaRepository unidadMedidaRepository,
            ArticuloInsumoRepository articuloInsumoRepository,
            ArticuloManufacturadoRepository articuloManufacturadoRepository) {
        return args -> {
            Categoria hamburguesas = categoriaRepository.save(Categoria.builder()
                    .denominacion("Hamburguesas")
                    .activo(true)
                    .build());

            Categoria bebidas = categoriaRepository.save(Categoria.builder()
                    .denominacion("Bebidas")
                    .activo(true)
                    .build());

            unidadMedidaRepository.save(UnidadMedida.builder()
                    .denominacion("Unidad")
                    .abreviatura("un")
                    .build());
            unidadMedidaRepository.save(UnidadMedida.builder()
                    .denominacion("Kilogramo")
                    .abreviatura("kg")
                    .build());

            articuloInsumoRepository.save(ArticuloInsumo.builder()
                    .nombre("Pan de hamburguesa")
                    .precioVenta(BigDecimal.valueOf(0))
                    .precioCosto(BigDecimal.valueOf(300))
                    .margen(0f)
                    .activo(true)
                    .esVendible(false)
                    .tiempoEstimadoMinutos(0)
                    .unidadMedidaEnum(UnidadMedidaEnum.UNIDAD)
                    .categoria(hamburguesas)
                    .stockActual(50.0)
                    .stockMinimo(10.0)
                    .stockMaximo(200.0)
                    .stockReservado(0.0)
                    .build());

            articuloManufacturadoRepository.save(ArticuloManufacturado.builder()
                    .nombre("Clásica")
                    .precioVenta(BigDecimal.valueOf(4500))
                    .precioCosto(BigDecimal.ZERO)
                    .activo(true)
                    .esVendible(true)
                    .tiempoEstimadoMinutos(15)
                    .unidadMedidaEnum(UnidadMedidaEnum.UNIDAD)
                    .categoria(hamburguesas)
                    .descripcion("Hamburguesa clásica con cheddar y panceta")
                    .preparacion("Cocinar el medallón a la plancha 4 minutos por lado")
                    .articuloManufacturadoDetalles(new ArrayList<>())
                    .build());

            articuloManufacturadoRepository.save(ArticuloManufacturado.builder()
                    .nombre("Coca-Cola 500ml")
                    .precioVenta(BigDecimal.valueOf(1200))
                    .precioCosto(BigDecimal.ZERO)
                    .activo(true)
                    .esVendible(true)
                    .tiempoEstimadoMinutos(0)
                    .unidadMedidaEnum(UnidadMedidaEnum.UNIDAD)
                    .categoria(bebidas)
                    .descripcion("Gaseosa línea Coca-Cola 500ml")
                    .preparacion("Servir fría")
                    .articuloManufacturadoDetalles(new ArrayList<>())
                    .build());
        };
    }
}
