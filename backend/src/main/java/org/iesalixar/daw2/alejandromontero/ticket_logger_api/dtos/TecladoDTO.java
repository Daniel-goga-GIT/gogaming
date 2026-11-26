package org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TecladoDTO extends ProductoDTO {

    @NotBlank(message = "Tipo de switch es obligatorio")
    private String tipoSwitch;

    @NotBlank(message = "Layout es obligatorio")
    private String layout;

    @NotBlank(message = "Conectividad es obligatoria")
    private String conectividad;

    private String retroiluminacion;
}
