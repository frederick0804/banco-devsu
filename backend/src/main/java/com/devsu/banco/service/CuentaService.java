package com.devsu.banco.service;

import com.devsu.banco.dto.CuentaDto;
import java.util.List;

public interface CuentaService {
    List<CuentaDto> findAll();
    CuentaDto findById(Long id);
    CuentaDto create(CuentaDto dto);
    CuentaDto update(Long id, CuentaDto dto);
    CuentaDto partialUpdate(Long id, CuentaDto dto);
    void delete(Long id);
}