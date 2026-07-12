package com.banco.ms_cuenta.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public  class ReporteDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstadoCuentaDTO {
        private String cliente;
        private String clienteId;
        private LocalDateTime fechaDesde;
        private LocalDateTime fechaHasta;
        private List<CuentaReporteDTO> cuentas;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CuentaReporteDTO {
        private String numeroCuenta;
        private String tipoCuenta;
        private BigDecimal saldoInicial;
        private BigDecimal saldoActual;
        private Boolean estado;
        private List<MovimientoReporteDTO> movimientos;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovimientoReporteDTO {
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime fecha;
        private String tipoMovimiento;
        private BigDecimal valor;
        private BigDecimal saldoDisponible;
    }
}
