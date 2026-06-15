package com.devsu.banco.repository;

import com.devsu.banco.entity.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    List<Movimiento> findByCuentaId(Long cuentaId);
    List<Movimiento> findByCuentaClienteIdAndFechaBetween(Long clienteId, LocalDateTime inicio, LocalDateTime fin);
    List<Movimiento> findByCuentaIdAndFechaBetween(Long cuentaId, LocalDateTime inicio, LocalDateTime fin);
}