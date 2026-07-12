package com.banco.ms_cuenta.integration;

import com.banco.ms_cuenta.dto.CuentaRequestDTO;
import com.banco.ms_cuenta.dto.MovimientoRequestDTO;
import com.banco.ms_cuenta.entities.ClienteReplica;
import com.banco.ms_cuenta.repositories.ClienteReplicaRepository;
import com.banco.ms_cuenta.repositories.CuentaRepository;
import com.banco.ms_cuenta.repositories.MovimientoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integracion (F6) que levanta el contexto completo de Spring
 * (controller + service + repository + base de datos H2) y valida el flujo
 * de extremo a extremo descrito en los casos de uso de la prueba tecnica:
 * creacion de cuenta, registro de movimientos, actualizacion de saldo,
 * rechazo por "Saldo no disponible" y generacion del reporte (F4).
 *
 * La replica local del cliente (normalmente poblada de forma asincrona por
 * RabbitMQ desde ms-cliente) se inserta directamente para no depender de un
 * broker de mensajeria durante la prueba.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CuentaMovimientoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteReplicaRepository clienteReplicaRepository;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @BeforeEach
    void setUp() {
        movimientoRepository.deleteAll();
        cuentaRepository.deleteAll();
        clienteReplicaRepository.deleteAll();
        clienteReplicaRepository.save(ClienteReplica.builder()
                .clientePersonaId(1L)
                .clienteId("mmontalvo")
                .nombre("Marianela Montalvo")
                .estado(true)
                .build());
    }

    @Test
    void flujoCompletoCrearCuentaRegistrarMovimientosYGenerarReporte() throws Exception {
        CuentaRequestDTO cuentaRequest = CuentaRequestDTO.builder()
                .numeroCuenta("225487")
                .tipoCuenta("CORRIENTE")
                .saldoInicial(new BigDecimal("100.00"))
                .estado(true)
                .clienteId("mmontalvo")
                .build();

        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cuentaRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldoActual").value(100.00));

        MovimientoRequestDTO deposito = MovimientoRequestDTO.builder()
                .numeroCuenta("225487")
                .tipoMovimiento("DEPOSITO")
                .valor(new BigDecimal("600.00"))
                .build();

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deposito)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldo").value(700.00));

        MovimientoRequestDTO retiroExcesivo = MovimientoRequestDTO.builder()
                .numeroCuenta("225487")
                .tipoMovimiento("RETIRO")
                .valor(new BigDecimal("5000.00"))
                .build();

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(retiroExcesivo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Saldo no disponible"));

        String hoy = java.time.LocalDate.now().toString();
        mockMvc.perform(get("/reportes")
                        .param("clienteId", "mmontalvo")
                        .param("fecha", hoy + "," + hoy))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente").value("Marianela Montalvo"))
                .andExpect(jsonPath("$.cuentas[0].numeroCuenta").value("225487"))
                .andExpect(jsonPath("$.cuentas[0].saldoActual").value(700.00))
                .andExpect(jsonPath("$.cuentas[0].movimientos.length()").value(1));
    }
}
