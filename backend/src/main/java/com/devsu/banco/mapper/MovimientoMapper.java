package com.devsu.banco.mapper;

import com.devsu.banco.dto.MovimientoDto;
import com.devsu.banco.entity.Movimiento;
import org.springframework.stereotype.Component;

@Component
public class MovimientoMapper implements Mapper<Movimiento, MovimientoDto> {

    @Override
    public MovimientoDto toDto(Movimiento movement) {
        MovimientoDto dto = new MovimientoDto();
        dto.setId(movement.getId());
        dto.setFecha(movement.getFecha());
        dto.setTipoMovimiento(movement.getTipoMovimiento());
        dto.setValor(movement.getValor());
        dto.setSaldo(movement.getSaldo());
        dto.setCuentaId(movement.getCuenta().getId());
        return dto;
    }

    @Override
    public Movimiento toEntity(MovimientoDto dto) {
        Movimiento movement = new Movimiento();
        movement.setTipoMovimiento(dto.getTipoMovimiento());
        movement.setValor(dto.getValor());
        return movement;
    }
}
