package com.banco.ms_cuenta.repositories;

import com.banco.ms_cuenta.entities.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    Optional<Movimiento> findFirstByCuentaIdOrderByFechaDescIdDesc(Long cuentaId);
    List<Movimiento> findByCuentaIdInAndFechaBetweenOrderByFechaAsc(List<Long> cuentaIds, LocalDateTime desde, LocalDateTime hasta);
}
