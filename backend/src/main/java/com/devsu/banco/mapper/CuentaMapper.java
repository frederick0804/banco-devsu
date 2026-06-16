package com.devsu.banco.mapper;

import com.devsu.banco.dto.CuentaDto;
import com.devsu.banco.entity.Cuenta;
import org.springframework.stereotype.Component;

@Component
public class CuentaMapper implements Mapper<Cuenta, CuentaDto> {

    @Override
    public CuentaDto toDto(Cuenta account) {
        CuentaDto dto = new CuentaDto();
        dto.setId(account.getId());
        dto.setNumeroCuenta(account.getNumeroCuenta());
        dto.setTipoCuenta(account.getTipoCuenta());
        dto.setSaldoInicial(account.getSaldoInicial());
        dto.setEstado(account.getEstado());
        dto.setClienteId(account.getCliente().getId());
        return dto;
    }

    @Override
    public Cuenta toEntity(CuentaDto dto) {
        Cuenta account = new Cuenta();
        account.setNumeroCuenta(dto.getNumeroCuenta());
        account.setTipoCuenta(dto.getTipoCuenta());
        account.setSaldoInicial(dto.getSaldoInicial());
        account.setEstado(dto.getEstado());
        return account;
    }
}
