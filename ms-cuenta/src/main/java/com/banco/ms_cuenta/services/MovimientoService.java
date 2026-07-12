package com.banco.ms_cuenta.services;

import com.banco.ms_cuenta.dto.MovimientoRequestDTO;
import com.banco.ms_cuenta.dto.MovimientoResponseDTO;
import com.banco.ms_cuenta.entities.Cuenta;
import com.banco.ms_cuenta.entities.Movimiento;
import com.banco.ms_cuenta.exceptions.BusinessException;
import com.banco.ms_cuenta.exceptions.ResourceNotFoundException;
import com.banco.ms_cuenta.exceptions.SaldoNoDisponibleException;
import com.banco.ms_cuenta.repositories.CuentaRepository;
import com.banco.ms_cuenta.repositories.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MovimientoService {

    private static final Set<String> TIPOS_VALIDOS = Set.of("DEPOSITO", "RETIRO");

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;

    @Transactional(readOnly = true)
    public MovimientoResponseDTO obtenerPorId(Long id) {
        return toResponseDTO(buscarMovimientoOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponseDTO> listarTodos() {
        return movimientoRepository.findAll().stream().map(this::toResponseDTO).toList();
    }

    @Transactional
    public MovimientoResponseDTO registarMovimiento(MovimientoRequestDTO body) {
        String tipo = body.getTipoMovimiento() == null ? null : body.getTipoMovimiento().toUpperCase();

        if (tipo == null || !TIPOS_VALIDOS.contains(tipo)) {
            throw new BusinessException("Tipo de movimiento no valido");
        }

        if (body.getValor() == null || body.getValor().compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException("Valor de movimiento no valido");
        }

        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(body.getNumeroCuenta()).orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada [" + body.getNumeroCuenta() + "]"));

        if(Boolean.FALSE.equals(cuenta.getEstado())) {
            throw new BusinessException("La cuneta inactiva [" + body.getNumeroCuenta() + "]");
        }

        BigDecimal valorAbsoluto = body.getValor().abs();
        BigDecimal valorAplicado = "RETIRO".equals(tipo) ? valorAbsoluto.negate() : valorAbsoluto;

        BigDecimal nuevoSaldo = cuenta.getSaldoActual().add(valorAplicado);

        if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new SaldoNoDisponibleException();
        }

        Movimiento movimiento = Movimiento.builder()
                .fecha(LocalDateTime.now())
                .tipoMovimiento(tipo)
                .valor(valorAplicado)
                .saldo(nuevoSaldo)
                .cuenta(cuenta)
                .build();

        movimientoRepository.save(movimiento);

        cuenta.setSaldoActual(nuevoSaldo);
        cuentaRepository.save(cuenta);
        return toResponseDTO(movimiento);
    }

    @Transactional
    public void eliminar(Long id) {
        Movimiento movimiento = buscarMovimientoOrThrow(id);
        movimientoRepository.delete(movimiento);
    }

    protected Movimiento buscarMovimientoOrThrow(Long id) {
        return movimientoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado: [" + id + " ]"));
    }

    private MovimientoResponseDTO toResponseDTO(Movimiento movimiento) {
        return MovimientoResponseDTO.builder()
                .id(movimiento.getId())
                .fecha(movimiento.getFecha())
                .tipoMovimiento(movimiento.getTipoMovimiento())
                .valor(movimiento.getValor())
                .saldo(movimiento.getSaldo())
                .numeroCuenta(movimiento.getCuenta().getNumeroCuenta())
                .build();
    }
}

