package com.banco.ms_cliente.repositories;

import com.banco.ms_cliente.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByClienteId(String clienteId);
    boolean existsByIdentificacion(String identificacion);
}
