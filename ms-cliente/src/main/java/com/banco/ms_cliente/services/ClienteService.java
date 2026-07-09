package com.banco.ms_cliente.services;

import com.banco.ms_cliente.dto.ClienteResponseDTO;
import com.banco.ms_cliente.entities.Cliente;
import com.banco.ms_cliente.repositories.ClienteRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {
    private final ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarTodos() {
        return clienteRepository.findAll().stream().map(this::toResponseDTO).toList();
    }


    private ClienteResponseDTO toResponseDTO(Cliente cliente) {
     return ClienteResponseDTO.builder()
             .id(cliente.getId())
             .nombre(cliente.getNombre())
             .genero(cliente.getGenero())
             .edad(cliente.getEdad())
             .identificacion(cliente.getIdentificacion())
             .direccion(cliente.getDireccion())
             .telefono(cliente.getTelefono())
             .clienteId(cliente.getClienteId())
             .estado(cliente.getEstado())
             .build();
    }
}
