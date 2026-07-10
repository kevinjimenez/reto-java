package com.banco.ms_cliente.services;

import com.banco.ms_cliente.dto.ClienteRequestDTO;
import com.banco.ms_cliente.dto.ClienteResponseDTO;
import com.banco.ms_cliente.entities.Cliente;
import com.banco.ms_cliente.exceptions.BusinessException;
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

    public  ClienteResponseDTO crear(ClienteRequestDTO request) {
        if(clienteRepository.existsByClienteId(request.getClienteId())) {
            throw new BusinessException("El cliente ya existe: [" + request.getClienteId() + "]");
        }

        if(clienteRepository.existsByIdentificacion(request.getIdentificacion())) {
            throw new BusinessException("La persona ya existe: [" + request.getIdentificacion() + "]" );
        }

        Cliente cliente = Cliente.builder()
                .nombre(request.getNombre())
                .genero(request.getGenero())
                .edad(request.getEdad())
                .identificacion(request.getIdentificacion())
                .direccion(request.getDireccion())
                .telefono(request.getTelefono())
                .clienteId(request.getClienteId())
                .contrasena(request.getContrasena())
                .estado(request.getEstado())
                .build();

        clienteRepository.save(cliente);

        //TODO: event
        return toResponseDTO(cliente);
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
