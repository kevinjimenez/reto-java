package com.banco.ms_cuenta.repositories;

import com.banco.ms_cuenta.entities.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
//    List<Movimiento> findByCuentaIdAndFechaBetweenOrderByFechaAsc(List<Long> clienteId, LocalDateTime desde, LocalDateTime hasta);
}
