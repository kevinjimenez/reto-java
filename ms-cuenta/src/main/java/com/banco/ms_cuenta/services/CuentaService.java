package com.banco.ms_cuenta.services;

import com.banco.ms_cuenta.dto.CuentaRequestDTO;
import com.banco.ms_cuenta.dto.CuentaResponseDTO;
import com.banco.ms_cuenta.entities.Cuenta;
import com.banco.ms_cuenta.exceptions.BusinessException;
import com.banco.ms_cuenta.exceptions.ResourceNotFoundException;
import com.banco.ms_cuenta.repositories.ClienteReplicaRepository;
import com.banco.ms_cuenta.repositories.CuentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CuentaService {
    private final CuentaRepository cuentaRepository;
    private final ClienteReplicaRepository clienteReplicaRepository;

    @Transactional(readOnly = true)
    public CuentaResponseDTO obtenerPorId(Long id) {
        return toResponseDTO(buscarCuentaOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<CuentaResponseDTO> listarTodos() {
        return cuentaRepository.findAll().stream().map(this::toResponseDTO).toList();
    }

    @Transactional
    public CuentaResponseDTO crear(CuentaRequestDTO request) {
        if (cuentaRepository.existsByNumeroCuenta(request.getNumeroCuenta())) {
            throw new BusinessException("La cuenta ya existe: [" + request.getNumeroCuenta() + "]");
        }

        clienteReplicaRepository.findByClienteId(request.getClienteId())
                .orElseThrow(() -> new BusinessException("La clienteId '" + request.getClienteId() +"' no ecist o aun no ha sincronizado"));

        Cuenta cuenta = Cuenta.builder()
                .numeroCuenta(request.getNumeroCuenta())
                .tipoCuenta(request.getTipoCuenta())
                .saldoInicial(request.getSaldoInicial())
                .saldoActual(request.getSaldoInicial())
                .estado(request.getEstado())
                .clienteId(request.getClienteId())
                .build();

        return toResponseDTO(cuentaRepository.save(cuenta));
    }

    @Transactional
    public CuentaResponseDTO actualizar(Long id, CuentaRequestDTO body) {
        Cuenta cuenta = buscarCuentaOrThrow(id);

        cuentaRepository.findByNumeroCuenta(body.getNumeroCuenta()).ifPresent(existing -> {
            if (!
                    existing.getId().equals(id)) {
                throw new BusinessException("Ya existe una cuenta con numero: " + body.getNumeroCuenta());
            }
        });

        cuenta.setNumeroCuenta(body.getNumeroCuenta());
        cuenta.setTipoCuenta(body.getTipoCuenta());
        cuenta.setEstado(body.getEstado());
        cuenta.setClienteId(body.getClienteId());

        return toResponseDTO(cuentaRepository.save(cuenta));
    }


    @Transactional
    public void eliminar(Long id) {
        Cuenta cuenta = buscarCuentaOrThrow(id);
        cuentaRepository.delete(cuenta);
    }


    protected Cuenta buscarCuentaOrThrow(Long id) {
        return cuentaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrado: [" + id + " ]"));
    }

    private CuentaResponseDTO toResponseDTO(Cuenta cuenta) {
        return CuentaResponseDTO.builder()
                .id(cuenta.getId())
                .numeroCuenta(cuenta.getNumeroCuenta())
                .tipoCuenta(cuenta.getTipoCuenta())
                .saldoInicial(cuenta.getSaldoInicial())
                .saldoActual(cuenta.getSaldoActual())
                .estado(cuenta.getEstado())
                .clienteId(cuenta.getClienteId())
                .build();
    }
}
