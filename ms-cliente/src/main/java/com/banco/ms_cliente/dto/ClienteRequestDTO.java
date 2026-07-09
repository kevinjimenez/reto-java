package com.banco.ms_cliente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRequestDTO {

    @NotBlank(message = "")
    private String nombre;

    @NotBlank(message = "")
    private String genero;

    @NotBlank(message = "")
    @Positive(message = "")
    private String edad;

    @NotBlank(message = "")
    private String identificacion;

    private String direccion;

    private String telefono;

    @NotBlank(message = "")
    private String clienteId;

    @NotBlank(message = "")
    private String contrasena;

    @NotBlank(message = "")
    private Boolean estado;
}
