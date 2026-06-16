package com.devsu.banco.repository;

import com.devsu.banco.entity.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    List<Movimiento> findByCuentaId(Long accountId);
    List<Movimiento> findByCuentaClienteIdAndFechaBetween(Long clientId, LocalDateTime start, LocalDateTime end);
    List<Movimiento> findByCuentaIdAndFechaBetween(Long accountId, LocalDateTime start, LocalDateTime end);
}
