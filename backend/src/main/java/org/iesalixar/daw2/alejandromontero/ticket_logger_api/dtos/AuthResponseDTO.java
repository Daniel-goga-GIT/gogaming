package org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String message;
    //Metemos uno nuevo para el Factor 2FA
    private boolean twofactor;
}
