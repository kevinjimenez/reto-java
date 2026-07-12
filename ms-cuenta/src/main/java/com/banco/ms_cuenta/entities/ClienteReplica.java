package com.banco.ms_cuenta.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clientes_replicas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteReplica {

    @Id
    private Long clientePersonaId;

    @Column(nullable = false, unique = true, length = 30)
    private String clienteId;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false)
    private Boolean estado;
}
