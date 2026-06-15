package com.devsu.banco.service;

import com.devsu.banco.dto.ClienteDto;
import java.util.List;

public interface ClienteService {
    List<ClienteDto> findAll();
    ClienteDto findById(Long id);
    ClienteDto create(ClienteDto dto);
    ClienteDto update(Long id, ClienteDto dto);
    ClienteDto partialUpdate(Long id, ClienteDto dto);
    void delete(Long id);
}