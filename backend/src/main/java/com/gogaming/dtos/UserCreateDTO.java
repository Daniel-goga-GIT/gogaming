package com.gogaming.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

/** Datos del formulario de registro (multipart, por la foto opcional). Mismas reglas que el frontend. */
@Data
@NoArgsConstructor // necesario para @ModelAttribute
@AllArgsConstructor
public class UserCreateDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 25, message = "El nombre admite 25 caracteres como máximo")
    private String firstName;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 50, message = "Los apellidos admiten 50 caracteres como máximo")
    private String lastName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es válido")
    @Size(max = 50, message = "El email admite 50 caracteres como máximo")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 50, message = "La contraseña debe tener entre 8 y 50 caracteres")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).*$", message = "La contraseña necesita al menos una mayúscula y un número")
    @ToString.Exclude
    private String password;

    @ToString.Exclude
    private MultipartFile imageFile;
}
