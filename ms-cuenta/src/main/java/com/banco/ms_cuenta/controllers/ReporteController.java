package com.banco.ms_cuenta.controllers;

import com.banco.ms_cuenta.dto.ReporteDTO;
import com.banco.ms_cuenta.services.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReporteController {
    private final ReporteService reporteService;

    @GetMapping("/reportes")
    public ResponseEntity<ReporteDTO.EstadoCuentaDTO> generarReporte(
            @RequestParam String clienteId,
            @RequestParam String fecha
    ) {
        return ResponseEntity.ok(reporteService.generarReporte(clienteId, fecha));
    }
}
