package com.banco.ms_cuenta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoRequestDTO {

    @NotBlank(message = "")
    private String numeroCuenta;

    @NotBlank(message = "")
    private String tipoMovimiento;

    @NotNull(message = "")
    private BigDecimal valor;
}
