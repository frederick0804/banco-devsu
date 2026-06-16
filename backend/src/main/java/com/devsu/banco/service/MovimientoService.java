package com.devsu.banco.service;

import com.devsu.banco.dto.MovimientoDto;
import com.devsu.banco.dto.ReporteDto;
import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoService {
    List<MovimientoDto> findAll();
    MovimientoDto findById(Long id);
    MovimientoDto create(MovimientoDto dto);
    MovimientoDto update(Long id, MovimientoDto dto);
    MovimientoDto partialUpdate(Long id, MovimientoDto dto);
    void delete(Long id);
    List<ReporteDto> getReport(Long clienteId, LocalDateTime start, LocalDateTime end);
    String getReportPdf(Long clienteId, LocalDateTime start, LocalDateTime end);
}
