package com.gogaming.dtos;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AlfombrillaDTO extends ProductoDTO {

    @NotBlank(message = "Dimensiones son obligatorias")
    private String dimensiones;

    private BigDecimal grosor;

    @NotBlank(message = "Superficie es obligatoria")
    private String superficie;
}
