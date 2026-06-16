package com.devsu.banco.service.impl;

import com.devsu.banco.dto.CuentaDto;
import com.devsu.banco.entity.Cliente;
import com.devsu.banco.entity.Cuenta;
import com.devsu.banco.exception.ResourceNotFoundException;
import com.devsu.banco.mapper.CuentaMapper;
import com.devsu.banco.repository.ClienteRepository;
import com.devsu.banco.repository.CuentaRepository;
import com.devsu.banco.service.CuentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;
    private final CuentaMapper cuentaMapper;

    @Override
    public List<CuentaDto> findAll() {
        return cuentaRepository.findAll().stream()
                .map(cuentaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CuentaDto findById(Long id) {
        return cuentaMapper.toDto(findAccountById(id));
    }

    @Override
    @Transactional
    public CuentaDto create(CuentaDto dto) {
        Cliente client = findClientById(dto.getClienteId());
        Cuenta account = cuentaMapper.toEntity(dto);
        account.setCliente(client);
        return cuentaMapper.toDto(cuentaRepository.save(account));
    }

    @Override
    @Transactional
    public CuentaDto update(Long id, CuentaDto dto) {
        Cuenta account = findAccountById(id);
        account.setNumeroCuenta(dto.getNumeroCuenta());
        account.setTipoCuenta(dto.getTipoCuenta());
        account.setSaldoInicial(dto.getSaldoInicial());
        account.setEstado(dto.getEstado());
        account.setCliente(findClientById(dto.getClienteId()));
        return cuentaMapper.toDto(cuentaRepository.save(account));
    }

    @Override
    @Transactional
    public CuentaDto partialUpdate(Long id, CuentaDto dto) {
        Cuenta account = findAccountById(id);
        if (dto.getNumeroCuenta() != null) account.setNumeroCuenta(dto.getNumeroCuenta());
        if (dto.getTipoCuenta() != null)   account.setTipoCuenta(dto.getTipoCuenta());
        if (dto.getSaldoInicial() != null) account.setSaldoInicial(dto.getSaldoInicial());
        if (dto.getEstado() != null)       account.setEstado(dto.getEstado());
        if (dto.getClienteId() != null)    account.setCliente(findClientById(dto.getClienteId()));
        return cuentaMapper.toDto(cuentaRepository.save(account));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findAccountById(id);
        cuentaRepository.deleteById(id);
    }

    private Cuenta findAccountById(Long id) {
        return cuentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id: " + id));
    }

    private Cliente findClientById(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
    }
}
