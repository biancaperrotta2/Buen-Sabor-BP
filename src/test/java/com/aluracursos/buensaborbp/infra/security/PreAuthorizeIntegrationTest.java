package com.aluracursos.buensaborbp.infra.security;

import com.aluracursos.buensaborbp.entity.Cliente;
import com.aluracursos.buensaborbp.entity.Empleado;
import com.aluracursos.buensaborbp.entity.Enums.Estado;
import com.aluracursos.buensaborbp.entity.Enums.EstadoPago;
import com.aluracursos.buensaborbp.entity.Enums.FormaPago;
import com.aluracursos.buensaborbp.entity.Enums.Rol;
import com.aluracursos.buensaborbp.entity.Enums.TipoEnvio;
import com.aluracursos.buensaborbp.entity.Pedido;
import com.aluracursos.buensaborbp.entity.Usuario;
import com.aluracursos.buensaborbp.repository.ClienteRepository;
import com.aluracursos.buensaborbp.repository.EmpleadoRepository;
import com.aluracursos.buensaborbp.repository.PedidoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Primera prueba de integración real del proyecto (hasta ahora todo era unitario con Mockito,
// ver "Automated tests" en CLAUDE.md): levanta el contexto completo de Spring contra el perfil
// h2 (base en memoria, nunca tocada hasta ahora) y ejercita la cadena completa —
// SecurityFilter (JWT real vía TokenService) + las reglas @PreAuthorize declarativas
// (hasRole/hasAnyRole) + las dos reglas con lógica propia (PedidoSecurity.esDelCliente,
// ClienteSecurity.esPropio) — end to end, tal como lo hace un browser real. Alcance elegido
// con el usuario: representativo del mecanismo general (401/403/200) más cobertura completa
// de las reglas custom, no un test por cada uno de los ~50 @PreAuthorize del proyecto (que en
// su enorme mayoría son variaciones del mismo hasRole/hasAnyRole ya cubierto acá).
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class PreAuthorizeIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private EmpleadoRepository empleadoRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private TokenService tokenService;

    private Cliente crearCliente(String usuario) {
        return clienteRepository.save(Cliente.builder()
                .nombre("Cliente").apellido("Test").usuario(usuario).password("x")
                .email(usuario + "@test.com").telefono("1111").rol(Rol.CLIENTE)
                .fechaNacimiento(LocalDate.of(2000, 1, 1)).activo(true)
                .build());
    }

    private Empleado crearEmpleado(String usuario, Rol rol, boolean activo) {
        return empleadoRepository.save(Empleado.builder()
                .nombre("Empleado").apellido("Test").usuario(usuario).password("x")
                .email(usuario + "@test.com").telefono("1111").rol(rol).activo(activo)
                .build());
    }

    private Pedido crearPedidoDe(Cliente cliente) {
        return pedidoRepository.save(Pedido.builder()
                .estado(Estado.PENDIENTE).estadoPago(EstadoPago.PENDIENTE)
                .tipoEnvio(TipoEnvio.TAKE_AWAY).formaPago(FormaPago.EFECTIVO)
                .fechaPedido(LocalDate.now())
                .cliente(cliente)
                .build());
    }

    private String tokenDe(Usuario usuario) {
        return tokenService.generarToken(usuario);
    }

    @Nested
    @DisplayName("Mecanismo general (401 / 403 / 200)")
    class MecanismoGeneral {
        @Test
        @DisplayName("Sin token, un endpoint protegido responde 401")
        void sinToken_devuelve401() throws Exception {
            mockMvc.perform(get("/clientes/1"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Con rol insuficiente, un endpoint ADMIN-only responde 403")
        void rolInsuficiente_devuelve403() throws Exception {
            Empleado cocinero = crearEmpleado("cocinero1", Rol.COCINERO, true);

            mockMvc.perform(get("/clientes").header("Authorization", "Bearer " + tokenDe(cocinero)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        @DisplayName("Con el rol correcto (ADMIN), un endpoint ADMIN-only responde 200")
        void rolCorrecto_devuelve200() throws Exception {
            Empleado admin = crearEmpleado("admin1", Rol.ADMIN, true);

            mockMvc.perform(get("/clientes").header("Authorization", "Bearer " + tokenDe(admin)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("hasAnyRole acepta cualquiera de los roles listados, no solo ADMIN")
        void hasAnyRole_aceptaOtroRolListado() throws Exception {
            Empleado cajero = crearEmpleado("cajero1", Rol.CAJERO, true);

            // /pedidos/cliente/{id} es hasAnyRole('ADMIN','CAJERO'); un cliente inexistente
            // solo importa para el 200 (lista vacía), lo que se prueba acá es el rol.
            mockMvc.perform(get("/pedidos/cliente/999999").header("Authorization", "Bearer " + tokenDe(cajero)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Un token válido de una cuenta desactivada se trata como no autenticado (401)")
        void cuentaDesactivada_tokenValidoPeroTratadoComoNoAutenticado() throws Exception {
            Empleado admin = crearEmpleado("admin2", Rol.ADMIN, false); // desactivado

            mockMvc.perform(get("/clientes").header("Authorization", "Bearer " + tokenDe(admin)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("ClienteSecurity.esPropio (GET /clientes/{id})")
    class ClienteSecurityEsPropio {
        @Test
        @DisplayName("Un cliente puede ver su propio perfil")
        void clientePuedeVerSuPropioPerfil() throws Exception {
            Cliente propio = crearCliente("cliente1");

            mockMvc.perform(get("/clientes/" + propio.getId_usuario())
                            .header("Authorization", "Bearer " + tokenDe(propio)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Un cliente NO puede ver el perfil de otro cliente")
        void clienteNoPuedeVerPerfilAjeno() throws Exception {
            Cliente otro = crearCliente("cliente2");
            Cliente quienConsulta = crearCliente("cliente3");

            mockMvc.perform(get("/clientes/" + otro.getId_usuario())
                            .header("Authorization", "Bearer " + tokenDe(quienConsulta)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Un ADMIN puede ver el perfil de cualquier cliente, aunque no sea suyo")
        void adminPuedeVerCualquierPerfil() throws Exception {
            Cliente cliente = crearCliente("cliente5");
            Empleado admin = crearEmpleado("admin3", Rol.ADMIN, true);

            mockMvc.perform(get("/clientes/" + cliente.getId_usuario())
                            .header("Authorization", "Bearer " + tokenDe(admin)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("PedidoSecurity.esDelCliente (GET /pedidos/{id})")
    class PedidoSecurityEsDelCliente {
        @Test
        @DisplayName("El cliente dueño del pedido puede verlo, aunque no tenga rol de empleado")
        void clienteDuenioPuedeVerSuPedido() throws Exception {
            Cliente cliente = crearCliente("cliente6");
            Pedido pedido = crearPedidoDe(cliente);

            mockMvc.perform(get("/pedidos/" + pedido.getId())
                            .header("Authorization", "Bearer " + tokenDe(cliente)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Otro cliente no puede ver un pedido ajeno")
        void otroClienteNoPuedeVerPedidoAjeno() throws Exception {
            Cliente dueno = crearCliente("cliente7");
            Pedido pedido = crearPedidoDe(dueno);
            Cliente otro = crearCliente("cliente8");

            mockMvc.perform(get("/pedidos/" + pedido.getId())
                            .header("Authorization", "Bearer " + tokenDe(otro)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Un CAJERO puede ver cualquier pedido sin necesidad de ser el cliente")
        void cajeroPuedeVerCualquierPedido() throws Exception {
            Cliente cliente = crearCliente("cliente9");
            Pedido pedido = crearPedidoDe(cliente);
            Empleado cajero = crearEmpleado("cajero2", Rol.CAJERO, true);

            mockMvc.perform(get("/pedidos/" + pedido.getId())
                            .header("Authorization", "Bearer " + tokenDe(cajero)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Un REPARTIDOR puede ver cualquier pedido por el hasAnyRole, sin ser el cliente dueño")
        void repartidorPuedeVerCualquierPedidoPorSuRol() throws Exception {
            Cliente cliente = crearCliente("cliente10");
            Pedido pedido = crearPedidoDe(cliente);
            Empleado repartidor = crearEmpleado("repartidor1", Rol.REPARTIDOR, true);

            mockMvc.perform(get("/pedidos/" + pedido.getId())
                            .header("Authorization", "Bearer " + tokenDe(repartidor)))
                    .andExpect(status().isOk());
        }
    }
}
