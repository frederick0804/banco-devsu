package com.devsu.banco.controller;

import com.devsu.banco.dto.CuentaDto;
import com.devsu.banco.service.CuentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuentaService;

    @GetMapping
    public ResponseEntity<List<CuentaDto>> findAll() {
        return ResponseEntity.ok(cuentaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(cuentaService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CuentaDto> create(@Valid @RequestBody CuentaDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cuentaService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CuentaDto> update(@PathVariable Long id, @Valid @RequestBody CuentaDto dto) {
        return ResponseEntity.ok(cuentaService.update(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CuentaDto> partialUpdate(@PathVariable Long id, @RequestBody CuentaDto dto) {
        return ResponseEntity.ok(cuentaService.partialUpdate(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cuentaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}