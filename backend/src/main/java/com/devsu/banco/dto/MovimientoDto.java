package com.devsu.banco.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoDto {

    private Long id;

    private LocalDateTime fecha;

    @NotBlank
    private String tipoMovimiento;

    @NotNull
    private Double valor;

    private Double saldo;

    @NotNull
    private Long cuentaId;
}