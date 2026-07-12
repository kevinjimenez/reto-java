package com.banco.ms_cuenta.repositories;

import com.banco.ms_cuenta.entities.ClienteReplica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<ClienteReplica, Long> {
    Optional<ClienteReplica> findByClienteId(Long clienteId);
}
