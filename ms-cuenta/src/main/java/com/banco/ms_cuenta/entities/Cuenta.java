package com.banco.ms_cliente.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "clientes")
@PrimaryKeyJoinColumn(name = "persona_id")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Cliente extends Persona {
    @NotBlank(message = "El clienteId es obligatorio")
    @Column(name = "cliente_id", nullable = false, unique = true, length = 30)
    private String clienteId;

    @NotBlank(message = "La contrasena es obligatorio")
    @Column(nullable = false, length = 100)
    private String contrasena;

    @NotNull(message = "El estado es obligatorio")
    @Column(nullable = false)
    private Boolean estado;
}
