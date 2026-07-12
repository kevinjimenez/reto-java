package com.banco.ms_cuenta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaRequestDTO {

    @NotBlank(message = "")
    private String numeroCuenta;

    @NotBlank(message = "")
    private String tipoCuenta;

    @NotNull(message = "")
    @PositiveOrZero(message = "")
    private BigDecimal saldoInicial;

    @NotNull(message = "")
    private Boolean estado;

    @NotBlank(message = "")
    private String clienteId;
}
