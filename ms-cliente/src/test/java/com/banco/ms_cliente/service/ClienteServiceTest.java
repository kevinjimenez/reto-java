package com.banco.ms_cliente.service;

import com.banco.ms_cliente.dto.ClienteRequestDTO;
import com.banco.ms_cliente.dto.ClienteResponseDTO;
import com.banco.ms_cliente.entities.Cliente;
import com.banco.ms_cliente.exceptions.BusinessException;
import com.banco.ms_cliente.exceptions.ResourceNotFoundException;
import com.banco.ms_cliente.messaging.ClienteEventPublisher;
import com.banco.ms_cliente.repositories.ClienteRepository;
import com.banco.ms_cliente.services.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la entidad/servicio de dominio Cliente (F5).
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteEventPublisher eventPublisher;

    @InjectMocks
    private ClienteService clienteService;

    private ClienteRequestDTO requestDTO;
    private Cliente clienteGuardado;

    @BeforeEach
    void setUp() {
        requestDTO = ClienteRequestDTO.builder()
                .nombre("Jose Lema")
                .genero("Masculino")
                .edad(35)
                .identificacion("100200300")
                .direccion("Otavalo sn y principal")
                .telefono("098254785")
                .clienteId("jlema")
                .contrasena("1234")
                .estado(true)
                .build();

        clienteGuardado = Cliente.builder()
                .id(1L)
                .nombre("Jose Lema")
                .genero("Masculino")
                .edad(35)
                .identificacion("100200300")
                .direccion("Otavalo sn y principal")
                .telefono("098254785")
                .clienteId("jlema")
                .contrasena("1234")
                .estado(true)
                .build();
    }

    @Test
    void deberiaCrearClienteExitosamente() {
        when(clienteRepository.existsByClienteId("jlema")).thenReturn(false);
        when(clienteRepository.existsByIdentificacion("100200300")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteGuardado);

        ClienteResponseDTO respuesta = clienteService.crear(requestDTO);

        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getClienteId()).isEqualTo("jlema");
        assertThat(respuesta.getNombre()).isEqualTo("Jose Lema");
        verify(clienteRepository, times(1)).save(any(Cliente.class));
        verify(eventPublisher, times(1)).publicarCreado(any(Cliente.class));
    }

    @Test
    void noDeberiaCrearClienteConClienteIdDuplicado() {
        when(clienteRepository.existsByClienteId("jlema")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.crear(requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("jlema");

        verify(clienteRepository, never()).save(any(Cliente.class));
        verify(eventPublisher, never()).publicarCreado(any(Cliente.class));
    }

    @Test
    void deberiaLanzarExcepcionSiClienteNoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
