package org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCheckoutRequestDTO {

    @NotEmpty
    @Valid
    private List<CartItemRequestDTO> items;

    private String metodoPago;
}
