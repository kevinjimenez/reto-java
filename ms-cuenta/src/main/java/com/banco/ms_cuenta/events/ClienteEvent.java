package com.banco.ms_cuenta.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteEvent implements Serializable {
    private String tipoEvento;
    private Long clientePersonaId;
    private String clienteId;
    private String nombre;
    private Boolean estado;
}
