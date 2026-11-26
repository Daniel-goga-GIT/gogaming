package org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.TipoAgarre;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RatonDTO extends ProductoDTO {

    @NotNull(message = "Tipo de agarre es obligatorio")
    private TipoAgarre tipoAgarre;

    @NotBlank(message = "Medidas son obligatorias")
    private String medidas;

    @NotBlank(message = "Sensor es obligatorio")
    private String sensor;

    @NotNull(message = "Peso es obligatorio")
    private BigDecimal peso;

    @NotBlank(message = "Switches son obligatorios")
    private String switches;
}
