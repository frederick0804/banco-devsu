package com.devsu.banco.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CuentaDto {

    private Long id;

    @NotBlank
    private String numeroCuenta;

    @NotBlank
    private String tipoCuenta;

    @NotNull
    private Double saldoInicial;

    @NotNull
    private Boolean estado;

    @NotNull
    private Long clienteId;
}