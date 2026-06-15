package com.devsu.banco.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "cliente")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente extends Persona {

    @NotBlank
    @Column(name = "clienteid", unique = true, nullable = false)
    private String clienteId;

    @NotBlank
    @Column(nullable = false)
    private String contrasena;

    @Column(nullable = false)
    private Boolean estado;
}