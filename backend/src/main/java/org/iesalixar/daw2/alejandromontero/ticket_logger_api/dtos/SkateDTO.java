package org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SkateDTO extends ProductoDTO {

    @NotBlank(message = "Material es obligatorio")
    private String material;

    private BigDecimal grosor;

    @NotBlank(message = "Compatibilidad es obligatoria")
    private String compatibilidad;
}
