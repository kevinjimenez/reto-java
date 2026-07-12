package com.banco.ms_cuenta.controller;

import com.banco.ms_cuenta.controllers.MovimientoController;
import com.banco.ms_cuenta.dto.MovimientoRequestDTO;
import com.banco.ms_cuenta.dto.MovimientoResponseDTO;
import com.banco.ms_cuenta.exceptions.SaldoNoDisponibleException;
import com.banco.ms_cuenta.services.MovimientoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias de los endpoints de /movimientos (minimo 2).
 */
@WebMvcTest(MovimientoController.class)
class MovimientoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MovimientoService movimientoService;

    @Test
    void deberiaRegistrarMovimientoYRetornar201() throws Exception {
        MovimientoRequestDTO request = MovimientoRequestDTO.builder()
                .numeroCuenta("225487")
                .tipoMovimiento("DEPOSITO")
                .valor(new BigDecimal("600.00"))
                .build();

        MovimientoResponseDTO response = MovimientoResponseDTO.builder()
                .id(1L)
                .fecha(LocalDateTime.now())
                .tipoMovimiento("DEPOSITO")
                .valor(new BigDecimal("600.00"))
                .saldo(new BigDecimal("700.00"))
                .numeroCuenta("225487")
                .build();

        when(movimientoService.registarMovimiento(any(MovimientoRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldo").value(700.00))
                .andExpect(jsonPath("$.numeroCuenta").value("225487"));
    }

    @Test
    void deberiaRetornar400CuandoSaldoNoDisponible() throws Exception {
        MovimientoRequestDTO request = MovimientoRequestDTO.builder()
                .numeroCuenta("496825")
                .tipoMovimiento("RETIRO")
                .valor(new BigDecimal("5000.00"))
                .build();

        when(movimientoService.registarMovimiento(any(MovimientoRequestDTO.class)))
                .thenThrow(new SaldoNoDisponibleException());

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Saldo no disponible"));
    }
}
