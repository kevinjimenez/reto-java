package com.banco.ms_cliente.controller;

import com.banco.ms_cliente.controllers.ClienteController;
import com.banco.ms_cliente.dto.ClienteRequestDTO;
import com.banco.ms_cliente.dto.ClienteResponseDTO;
import com.banco.ms_cliente.exceptions.ResourceNotFoundException;
import com.banco.ms_cliente.services.ClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias de los endpoints de /clientes (minimo 2, segun
 * indicaciones generales de la prueba tecnica).
 */
@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClienteService clienteService;

    @Test
    void deberiaCrearClienteYRetornar201() throws Exception {
        ClienteRequestDTO request = ClienteRequestDTO.builder()
                .nombre("Marianela Montalvo")
                .genero("Femenino")
                .edad(28)
                .identificacion("100200301")
                .direccion("Amazonas y NNUU")
                .telefono("097548965")
                .clienteId("mmontalvo")
                .contrasena("5678")
                .estado(true)
                .build();

        ClienteResponseDTO response = ClienteResponseDTO.builder()
                .id(1L)
                .nombre("Marianela Montalvo")
                .genero("Femenino")
                .edad(28)
                .identificacion("100200301")
                .clienteId("mmontalvo")
                .estado(true)
                .build();

        when(clienteService.crear(any(ClienteRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value("mmontalvo"))
                .andExpect(jsonPath("$.nombre").value("Marianela Montalvo"));
    }

    @Test
    void deberiaRetornar404CuandoClienteNoExiste() throws Exception {
        when(clienteService.obtenerPorId(eq(999L)))
                .thenThrow(new ResourceNotFoundException("No se encontro el cliente con id: 999"));

        mockMvc.perform(get("/clientes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No se encontro el cliente con id: 999"));
    }

    @Test
    void deberiaRetornar400SiFaltaNombre() throws Exception {
        ClienteRequestDTO invalido = ClienteRequestDTO.builder()
                .genero("Masculino")
                .edad(30)
                .identificacion("100200302")
                .clienteId("test")
                .contrasena("1234")
                .estado(true)
                .build();

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }
}
