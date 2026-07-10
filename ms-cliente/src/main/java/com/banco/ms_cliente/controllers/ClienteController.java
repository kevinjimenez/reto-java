package com.banco.ms_cliente.controllers;

import com.banco.ms_cliente.dto.ClienteRequestDTO;
import com.banco.ms_cliente.dto.ClienteResponseDTO;
import com.banco.ms_cliente.services.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listar() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crear(@Valid @RequestBody ClienteRequestDTO request) {
        ClienteResponseDTO clienteResponseDTO = clienteService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteResponseDTO);
    }
}
