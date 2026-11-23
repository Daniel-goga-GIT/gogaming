package org.iesalixar.daw2.alejandromontero.ticket_logger_api.mappers;

import lombok.Getter;
import lombok.Setter;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.UserCreateDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.UserDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.User;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setImage(user.getImage());
        return dto;
    }

    public User toEntity(UserCreateDTO dto) {
        User user = new User();
        // No seteamos ID porque es una creación
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPassword(dto.getPassword());
        // Necesitas confirmar por correo electronico el gmail
        //De momento desactivado
        user.setEnabled(true);
        return user;
    }
}
