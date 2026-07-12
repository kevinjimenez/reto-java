package com.banco.ms_cuenta.service;

import com.banco.ms_cuenta.dto.MovimientoRequestDTO;
import com.banco.ms_cuenta.dto.MovimientoResponseDTO;
import com.banco.ms_cuenta.entities.Cuenta;
import com.banco.ms_cuenta.exceptions.SaldoNoDisponibleException;
import com.banco.ms_cuenta.repositories.CuentaRepository;
import com.banco.ms_cuenta.repositories.MovimientoRepository;
import com.banco.ms_cuenta.services.MovimientoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para las reglas de negocio de registro de movimientos
 * (F2: actualizacion de saldo, F3: "Saldo no disponible").
 */
@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    private MovimientoService movimientoService;

    private Cuenta cuenta;

    @BeforeEach
    void setUp() {
        movimientoService = new MovimientoService(movimientoRepository, cuentaRepository);
        cuenta = Cuenta.builder()
                .id(1L)
                .numeroCuenta("478758")
                .tipoCuenta("AHORRO")
                .saldoInicial(new BigDecimal("2000.00"))
                .saldoActual(new BigDecimal("2000.00"))
                .estado(true)
                .clienteId("jlema")
                .build();
    }

    @Test
    void deberiaRegistrarRetiroYActualizarSaldo() {
        when(cuentaRepository.findByNumeroCuenta("478758")).thenReturn(Optional.of(cuenta));

        MovimientoRequestDTO dto = MovimientoRequestDTO.builder()
                .numeroCuenta("478758")
                .tipoMovimiento("RETIRO")
                .valor(new BigDecimal("575.00"))
                .build();

        MovimientoResponseDTO respuesta = movimientoService.registarMovimiento(dto);

        assertThat(respuesta.getSaldo()).isEqualByComparingTo("1425.00");
        assertThat(cuenta.getSaldoActual()).isEqualByComparingTo("1425.00");

        ArgumentCaptor<Cuenta> cuentaCaptor = ArgumentCaptor.forClass(Cuenta.class);
        verify(cuentaRepository).save(cuentaCaptor.capture());
        assertThat(cuentaCaptor.getValue().getSaldoActual()).isEqualByComparingTo("1425.00");
        verify(movimientoRepository, times(1)).save(any());
    }

    @Test
    void deberiaLanzarSaldoNoDisponibleSiElRetiroSuperaElSaldo() {
        when(cuentaRepository.findByNumeroCuenta("478758")).thenReturn(Optional.of(cuenta));

        MovimientoRequestDTO dto = MovimientoRequestDTO.builder()
                .numeroCuenta("478758")
                .tipoMovimiento("RETIRO")
                .valor(new BigDecimal("5000.00"))
                .build();

        assertThatThrownBy(() -> movimientoService.registarMovimiento(dto))
                .isInstanceOf(SaldoNoDisponibleException.class)
                .hasMessage("Saldo no disponible");

        verify(movimientoRepository, never()).save(any());
        verify(cuentaRepository, never()).save(any());
    }

    @Test
    void deberiaRegistrarDepositoYSumarAlSaldo() {
        when(cuentaRepository.findByNumeroCuenta("478758")).thenReturn(Optional.of(cuenta));

        MovimientoRequestDTO dto = MovimientoRequestDTO.builder()
                .numeroCuenta("478758")
                .tipoMovimiento("DEPOSITO")
                .valor(new BigDecimal("600.00"))
                .build();

        MovimientoResponseDTO respuesta = movimientoService.registarMovimiento(dto);

        assertThat(respuesta.getSaldo()).isEqualByComparingTo("2600.00");
    }
}
