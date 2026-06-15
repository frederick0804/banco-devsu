package com.devsu.banco.controller;

import com.devsu.banco.dto.MovimientoDto;
import com.devsu.banco.dto.ReporteDto;
import com.devsu.banco.service.MovimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;

    @GetMapping("/movimientos")
    public ResponseEntity<List<MovimientoDto>> findAll() {
        return ResponseEntity.ok(movimientoService.findAll());
    }

    @GetMapping("/movimientos/{id}")
    public ResponseEntity<MovimientoDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(movimientoService.findById(id));
    }

    @PostMapping("/movimientos")
    public ResponseEntity<MovimientoDto> create(@Valid @RequestBody MovimientoDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movimientoService.create(dto));
    }

    @PutMapping("/movimientos/{id}")
    public ResponseEntity<MovimientoDto> update(@PathVariable Long id, @Valid @RequestBody MovimientoDto dto) {
        return ResponseEntity.ok(movimientoService.update(id, dto));
    }

    @PatchMapping("/movimientos/{id}")
    public ResponseEntity<MovimientoDto> partialUpdate(@PathVariable Long id, @RequestBody MovimientoDto dto) {
        return ResponseEntity.ok(movimientoService.partialUpdate(id, dto));
    }

    @DeleteMapping("/movimientos/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        movimientoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reportes")
    public ResponseEntity<List<ReporteDto>> getReporte(
            @RequestParam Long clienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        return ResponseEntity.ok(movimientoService.getReporte(clienteId, fechaInicio, fechaFin));
    }
    @GetMapping("/reportes/pdf")
    public ResponseEntity<Map<String, String>> getReportePdf(
            @RequestParam Long clienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        String pdf = movimientoService.getReportePdf(clienteId, fechaInicio, fechaFin);
        return ResponseEntity.ok(Map.of("pdf", pdf));
    }
}