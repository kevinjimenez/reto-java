package com.banco.ms_cuenta.services;

import com.banco.ms_cuenta.dto.ReporteDTO;
import com.banco.ms_cuenta.entities.ClienteReplica;
import com.banco.ms_cuenta.entities.Cuenta;
import com.banco.ms_cuenta.entities.Movimiento;
import com.banco.ms_cuenta.exceptions.BusinessException;
import com.banco.ms_cuenta.exceptions.ResourceNotFoundException;
import com.banco.ms_cuenta.repositories.ClienteReplicaRepository;
import com.banco.ms_cuenta.repositories.CuentaRepository;
import com.banco.ms_cuenta.repositories.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ISO_LOCAL_DATE;

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;
    private final ClienteReplicaRepository clienteReplicaRepository;

    @Transactional(readOnly = true)
    public ReporteDTO.EstadoCuentaDTO generarReporte(String clienteId, String rangoFechas) {
        ClienteReplica cliente = clienteReplicaRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el cliente [" + clienteId +"]"));

        String[] partes = rangoFechas.split(",");
        if(partes.length != 2){
            throw new BusinessException("Rango no valido");
        }

        LocalDateTime desde;
        LocalDateTime hasta;

        try {
            desde = LocalDate.parse(partes[0].trim(), FORMATO_FECHA).atStartOfDay();
            hasta = LocalDate.parse(partes[1].trim(), FORMATO_FECHA).atTime(23, 59, 59);
        } catch (Exception e) {
            throw new BusinessException("Formato de fecha no valido");
        }

        if(desde.isAfter(hasta)){
            throw new BusinessException("Rango no valido");
        }

        List<Cuenta> cuentas = cuentaRepository.findByClienteId(clienteId);
        List<Long> cuentaIds = cuentas.stream().map(Cuenta::getId).toList();

        Map<Long, List<Movimiento>> movimientosPorCuenta = cuentaIds.isEmpty()
                ? Map.of()
                : movimientoRepository.findByCuentaIdInAndFechaBetweenOrderByFechaAsc(cuentaIds, desde, hasta)
                  .stream()
                  .collect(Collectors.groupingBy(m -> m.getCuenta().getId()));


        List<ReporteDTO.CuentaReporteDTO> cuentasReporte = cuentas
                .stream()
                .sorted(Comparator.comparing(Cuenta::getNumeroCuenta))
                .map(cuenta -> ReporteDTO.CuentaReporteDTO.builder()
                        .numeroCuenta(cuenta.getNumeroCuenta())
                                .tipoCuenta(cuenta.getTipoCuenta())
                                .saldoInicial(cuenta.getSaldoInicial())
                                .saldoActual(cuenta.getSaldoActual())
                                .estado(cuenta.getEstado())
                                .movimientos(movimientosPorCuenta.getOrDefault(cuenta.getId(), List.of()).stream()
                                        .map(mov -> ReporteDTO.MovimientoReporteDTO.builder()
                                                .fecha(mov.getFecha())
                                                .tipoMovimiento(mov.getTipoMovimiento())
                                                .valor(mov.getValor())
                                                .saldoDisponible(mov.getSaldo())
                                                .build())
                                        .toList())
                                .build()
                        ).toList();

        return ReporteDTO.EstadoCuentaDTO.builder()
                .cliente(cliente.getNombre())
                .clienteId(cliente.getClienteId())
                .fechaDesde(desde)
                .fechaHasta(hasta)
                .cuentas(cuentasReporte)
                .build();

    }
}

