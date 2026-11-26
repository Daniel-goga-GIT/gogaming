package com.gogaming.controllers;

import jakarta.validation.Valid;
import com.gogaming.dtos.AuthRequestDTO;
import com.gogaming.dtos.AuthResponseDTO;
import com.gogaming.dtos.TwoFactorDTO;
import com.gogaming.entities.Role;
import com.gogaming.entities.User;
import com.gogaming.services.EmailService;
import com.gogaming.services.UserService;
import com.gogaming.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AuthenticationController {

    /** Generador criptográficamente seguro para los códigos 2FA. */
    private static final java.security.SecureRandom RANDOM = new java.security.SecureRandom();

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;



    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponseDTO> authenticate(@Valid @RequestBody AuthRequestDTO authRequest) {
        try {
            // Validar datos de entrada (opcional si no usas validación adicional en DTO)
            if (authRequest.getEmail() == null || authRequest.getPassword() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponseDTO(null, "El correo electronico y la contraseña son obligatorios.", false));
            }

            // Intenta autenticar al usuario con las credenciales proporcionadas
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );

            // Obtiene el nombre de usuario autenticado
            String email = authentication.getName();

            // Extrae los roles del usuario autenticado desde las autoridades asignadas
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority) // Convierte cada autoridad en su representación de texto
                    .toList();


            // Genera un token JWT para el usuario autenticado, incluyendo sus roles y el ID
            String token = jwtUtil.generateToken(email, roles, userService.getIdByEmail(email), true);

            //Modificamos para devolver un private
            String code = generateCode();
            try {
                emailService.sendTwoFactorCodeEmail(email, code);
            } catch (Exception e) {
                logger.error("Error al enviar el correo de 2FA a {}", email, e);
            }
            userService.saveCode(code, userService.getIdByEmail(email));

            // Retorna una respuesta con el token JWT y un mensaje de éxito
            return ResponseEntity.ok(new AuthResponseDTO(token, "Authentication successful first part", true));

        } catch (BadCredentialsException e) {
            // Manejo de credenciales inválidas
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDTO(null, "Credenciales inválidas. Por favor, verifica tus datos.", false));
        } catch (DisabledException e) {
            // Manejo de usuario no habilitado (correo no verificado)
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AuthResponseDTO(null, "El correo electrónico no está verificado.", false));
        } catch (Exception e) {
            // Manejo de cualquier otro error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponseDTO(null, "Ocurrió un error inesperado. Por favor, inténtalo de nuevo más tarde.", false));
        }
    }

    @PostMapping("/twofactor")
    public ResponseEntity<AuthResponseDTO> twofactor(@Valid @RequestBody TwoFactorDTO twoFactorDTO, @RequestHeader("Authorization") String tokenHeader) {
        logger.info("Iniciando two-factor authentication" + twoFactorDTO.toString());


        // 1. Limpiamos el prefijo "Bearer "
        String token = tokenHeader.replace("Bearer ", "");


        // 2. Usamos tu servicio JWT/Utilidad para extraer el ID
        Long id = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));


        User user =  userService.getUserById(id);
        if (user != null && java.util.Objects.equals(twoFactorDTO.getCode(), user.getCode())) {
            List<String> roles = user.getRoles().stream()
                    .map(Role::getName) // Ajusta según tu entidad User/Role
                    .toList();


            String tokenRenew = jwtUtil.generateToken(user.getEmail(), roles, id, false);
            userService.clear2FactorCode(id);

            return ResponseEntity.ok(new AuthResponseDTO(tokenRenew, "Autenticación exitosa", false));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthResponseDTO(null, "El código enviado no es correcto.", false));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponseDTO> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AuthResponseDTO(null, "Ocurrió un error inesperado: " + e.getMessage(), false));
    }


    //TEMA PARA EL 2FA GENERAR CORREO Y CÓDIGO
    private String generateCode(){
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

}
