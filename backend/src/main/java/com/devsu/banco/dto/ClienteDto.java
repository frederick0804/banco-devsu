package com.devsu.banco.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDto {

    private Long id;

    @NotBlank
    private String nombre;

    @NotBlank
    private String genero;

    @Min(0)
    private Integer edad;

    @NotBlank
    private String identificacion;

    private String direccion;

    private String telefono;

    @NotBlank
    private String clienteId;

    @NotBlank
    private String contrasena;

    @NotNull
    private Boolean estado;
}