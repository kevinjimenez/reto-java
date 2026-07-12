package com.banco.ms_cuenta.controller;

import com.banco.ms_cuenta.controllers.CuentaController;
import com.banco.ms_cuenta.dto.CuentaRequestDTO;
import com.banco.ms_cuenta.dto.CuentaResponseDTO;
import com.banco.ms_cuenta.exceptions.ResourceNotFoundException;
import com.banco.ms_cuenta.services.CuentaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias de los endpoints de /cuentas (minimo 2).
 */
@WebMvcTest(CuentaController.class)
class CuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CuentaService cuentaService;

    @Test
    void deberiaCrearCuentaYRetornar201() throws Exception {
        CuentaRequestDTO request = CuentaRequestDTO.builder()
                .numeroCuenta("495878")
                .tipoCuenta("AHORROS")
                .saldoInicial(BigDecimal.ZERO)
                .estado(true)
                .clienteId("josorio")
                .build();

        CuentaResponseDTO response = CuentaResponseDTO.builder()
                .id(1L)
                .numeroCuenta("495878")
                .tipoCuenta("AHORROS")
                .saldoInicial(BigDecimal.ZERO)
                .saldoActual(BigDecimal.ZERO)
                .estado(true)
                .clienteId("josorio")
                .build();

        when(cuentaService.crear(any(CuentaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCuenta").value("495878"));
    }

    @Test
    void deberiaRetornar404CuandoCuentaNoExiste() throws Exception {
        when(cuentaService.obtenerPorId(eq(123L)))
                .thenThrow(new ResourceNotFoundException("No se encontro la cuenta con id: 123"));

        mockMvc.perform(get("/cuentas/123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No se encontro la cuenta con id: 123"));
    }
}
