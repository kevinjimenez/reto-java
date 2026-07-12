package com.banco.ms_cuenta.controllers;

import com.banco.ms_cuenta.dto.CuentaRequestDTO;
import com.banco.ms_cuenta.dto.CuentaResponseDTO;
import com.banco.ms_cuenta.services.CuentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cuentas")
@RequiredArgsConstructor
public class CuentaController {
    private final CuentaService cuentaService;

    @GetMapping
    public ResponseEntity<List<CuentaResponseDTO>> listar() {
        return ResponseEntity.ok(cuentaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(cuentaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<CuentaResponseDTO> crear(@Valid @RequestBody CuentaRequestDTO request) {
        CuentaResponseDTO clienteResponseDTO = cuentaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteResponseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CuentaResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody CuentaRequestDTO dto) {
        CuentaResponseDTO clienteResponseDTO = cuentaService.actualizar(id, dto);
        return ResponseEntity.ok(clienteResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CuentaResponseDTO> eliminar(@PathVariable Long id) {
        cuentaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
