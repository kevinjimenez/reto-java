package com.banco.ms_cuenta.controllers;

import com.banco.ms_cuenta.dto.MovimientoRequestDTO;
import com.banco.ms_cuenta.dto.MovimientoResponseDTO;
import com.banco.ms_cuenta.services.MovimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movimientos")
@RequiredArgsConstructor
public class MovimientoController {
    private final MovimientoService movimientoService;

    @GetMapping
    public ResponseEntity<List<MovimientoResponseDTO>> listar() {
        return ResponseEntity.ok(movimientoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimientoResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(movimientoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<MovimientoResponseDTO> registar(@Valid @RequestBody MovimientoRequestDTO request) {
        MovimientoResponseDTO movimiento = movimientoService.registarMovimiento(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(movimiento);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        movimientoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
