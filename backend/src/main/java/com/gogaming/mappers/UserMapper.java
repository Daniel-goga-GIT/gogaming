package com.gogaming.mappers;

import lombok.Getter;
import lombok.Setter;
import com.gogaming.dtos.UserCreateDTO;
import com.gogaming.dtos.UserDTO;
import com.gogaming.entities.User;
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
