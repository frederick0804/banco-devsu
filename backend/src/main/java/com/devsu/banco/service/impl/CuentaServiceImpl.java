package com.devsu.banco.service.impl;

import com.devsu.banco.dto.CuentaDto;
import com.devsu.banco.entity.Cliente;
import com.devsu.banco.entity.Cuenta;
import com.devsu.banco.exception.ResourceNotFoundException;
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

    @Override
    public List<CuentaDto> findAll() {
        return cuentaRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CuentaDto findById(Long id) {
        return toDto(findCuentaById(id));
    }

    @Override
    @Transactional
    public CuentaDto create(CuentaDto dto) {
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + dto.getClienteId()));
        Cuenta cuenta = toEntity(dto, cliente);
        return toDto(cuentaRepository.save(cuenta));
    }

    @Override
    @Transactional
    public CuentaDto update(Long id, CuentaDto dto) {
        Cuenta cuenta = findCuentaById(id);
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + dto.getClienteId()));
        cuenta.setNumeroCuenta(dto.getNumeroCuenta());
        cuenta.setTipoCuenta(dto.getTipoCuenta());
        cuenta.setSaldoInicial(dto.getSaldoInicial());
        cuenta.setEstado(dto.getEstado());
        cuenta.setCliente(cliente);
        return toDto(cuentaRepository.save(cuenta));
    }

    @Override
    @Transactional
    public CuentaDto partialUpdate(Long id, CuentaDto dto) {
        Cuenta cuenta = findCuentaById(id);
        if (dto.getNumeroCuenta() != null) cuenta.setNumeroCuenta(dto.getNumeroCuenta());
        if (dto.getTipoCuenta() != null) cuenta.setTipoCuenta(dto.getTipoCuenta());
        if (dto.getSaldoInicial() != null) cuenta.setSaldoInicial(dto.getSaldoInicial());
        if (dto.getEstado() != null) cuenta.setEstado(dto.getEstado());
        if (dto.getClienteId() != null) {
            Cliente cliente = clienteRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + dto.getClienteId()));
            cuenta.setCliente(cliente);
        }
        return toDto(cuentaRepository.save(cuenta));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findCuentaById(id);
        cuentaRepository.deleteById(id);
    }

    private Cuenta findCuentaById(Long id) {
        return cuentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id: " + id));
    }

    private CuentaDto toDto(Cuenta cuenta) {
        CuentaDto dto = new CuentaDto();
        dto.setId(cuenta.getId());
        dto.setNumeroCuenta(cuenta.getNumeroCuenta());
        dto.setTipoCuenta(cuenta.getTipoCuenta());
        dto.setSaldoInicial(cuenta.getSaldoInicial());
        dto.setEstado(cuenta.getEstado());
        dto.setClienteId(cuenta.getCliente().getId());
        return dto;
    }

    private Cuenta toEntity(CuentaDto dto, Cliente cliente) {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(dto.getNumeroCuenta());
        cuenta.setTipoCuenta(dto.getTipoCuenta());
        cuenta.setSaldoInicial(dto.getSaldoInicial());
        cuenta.setEstado(dto.getEstado());
        cuenta.setCliente(cliente);
        return cuenta;
    }
}