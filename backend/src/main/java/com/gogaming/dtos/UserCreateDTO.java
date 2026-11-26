package com.gogaming.dtos;

import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Data
@NoArgsConstructor // VITAL para @ModelAttribute
@AllArgsConstructor
public class UserCreateDTO {

    @Size(max = 25)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Size(max = 50)
    private String email;

    @Size(max = 50)
    private String password;

    private MultipartFile imageFile;
}
