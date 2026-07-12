package com.banco.ms_cliente.services;

import com.banco.ms_cliente.dto.ClienteRequestDTO;
import com.banco.ms_cliente.dto.ClienteResponseDTO;
import com.banco.ms_cliente.entities.Cliente;
import com.banco.ms_cliente.exceptions.BusinessException;
import com.banco.ms_cliente.exceptions.ResourceNotFoundException;
import com.banco.ms_cliente.messaging.ClienteEventPublisher;
import com.banco.ms_cliente.repositories.ClienteRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final ClienteEventPublisher clienteEventPublisher;

    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerPorId(Long id) {
        Cliente cliente = buscarClienteOrThrow(id);
        return toResponseDTO(cliente);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarTodos() {
        return clienteRepository.findAll().stream().map(this::toResponseDTO).toList();
    }

    @Transactional
    public ClienteResponseDTO crear(ClienteRequestDTO request) {
        if (clienteRepository.existsByClienteId(request.getClienteId())) {
            throw new BusinessException("El cliente ya existe: [" + request.getClienteId() + "]");
        }

        if (clienteRepository.existsByIdentificacion(request.getIdentificacion())) {
            throw new BusinessException("La persona ya existe: [" + request.getIdentificacion() + "]");
        }

        Cliente cliente = Cliente.builder().nombre(request.getNombre()).genero(request.getGenero()).edad(request.getEdad()).identificacion(request.getIdentificacion()).direccion(request.getDireccion()).telefono(request.getTelefono()).clienteId(request.getClienteId()).contrasena(request.getContrasena()).estado(request.getEstado()).build();

        Cliente guardado = clienteRepository.save(cliente);
        clienteEventPublisher.publicarCreado(guardado);
        return toResponseDTO(cliente);
    }

    @Transactional
    public ClienteResponseDTO actualizar(Long id, ClienteRequestDTO dto) {
        Cliente cliente = buscarClienteOrThrow(id);

        clienteRepository.findByClienteId(dto.getClienteId()).ifPresent(existing -> {
            if (!
                    existing.getId().equals(id)) {
                throw new BusinessException("Ya existe un cliente con clienteId: " + dto.getClienteId());
            }
        });

        cliente.setNombre(dto.getNombre());
        cliente.setGenero(dto.getGenero());
        cliente.setEdad(dto.getEdad());
        cliente.setIdentificacion(dto.getIdentificacion());
        cliente.setDireccion(dto.getDireccion());
        cliente.setTelefono(dto.getTelefono());
        cliente.setClienteId(dto.getClienteId());
        cliente.setContrasena(dto.getContrasena());
        cliente.setEstado(dto.getEstado());
        Cliente actualizado = clienteRepository.save(cliente);
        clienteEventPublisher.publicarCreado(actualizado);
        return toResponseDTO(actualizado);
    }


    @Transactional
    public void eliminar(Long id) {
        Cliente cliente = buscarClienteOrThrow(id);
        clienteRepository.delete(cliente);
        clienteEventPublisher.publicarEliminado(cliente);
    }


    protected Cliente buscarClienteOrThrow(Long id) {
        return clienteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: [" + id + " ]"));
    }

    private ClienteResponseDTO toResponseDTO(Cliente cliente) {
        return ClienteResponseDTO.builder().id(cliente.getId()).nombre(cliente.getNombre()).genero(cliente.getGenero()).edad(cliente.getEdad()).identificacion(cliente.getIdentificacion()).direccion(cliente.getDireccion()).telefono(cliente.getTelefono()).clienteId(cliente.getClienteId()).estado(cliente.getEstado()).build();
    }
}
