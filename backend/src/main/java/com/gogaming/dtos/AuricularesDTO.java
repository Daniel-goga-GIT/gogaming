package com.gogaming.dtos;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuricularesDTO extends ProductoDTO {

    @NotBlank(message = "Tipo es obligatorio")
    private String tipo;

    @NotBlank(message = "Driver es obligatorio")
    private String driver;

    @NotBlank(message = "Conectividad es obligatoria")
    private String conectividad;

    private String impedancia;
}
