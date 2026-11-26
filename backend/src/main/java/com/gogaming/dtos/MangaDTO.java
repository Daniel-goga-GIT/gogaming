package com.gogaming.dtos;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MangaDTO extends ProductoDTO {

    @NotBlank(message = "Talla es obligatoria")
    private String talla;

    @NotBlank(message = "Material es obligatorio")
    private String material;

    private BigDecimal largo;

    @NotBlank(message = "Color es obligatorio")
    private String color;
}
