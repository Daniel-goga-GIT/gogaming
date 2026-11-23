package org.iesalixar.daw2.alejandromontero.ticket_logger_api.controllers;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.apache.coyote.Request;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.UserCreateDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.UserDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.ResenaResponseDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.User;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.mappers.UserMapper;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories.UserRepository;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.services.FileStorageService;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.services.ResenaService;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.services.UserService;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResenaService resenaService;

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        logger.info("Solicitando la lista de todos los usuarios...");
        try {
            List<UserDTO> usuarios = userService.getAllUsers();
            logger.info("Se han encontrado {} usuarios", usuarios.size());
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            logger.error("Error al obtener los usuarios de la lista", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/register", consumes = { "multipart/form-data", "multipart/form-data;*" })
    public ResponseEntity<?> createUser(
            @Valid @ModelAttribute UserCreateDTO userCreateDTO
        ) {
        try {
            UserDTO createUser = userService.createUser(userCreateDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createUser);
        } catch (IllegalArgumentException e) {
            logger.warn("Error al crear el usuario: {}",  e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Error al guardar la imagen: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar la imagen");
        }
        catch (Exception e) {
            logger.error("Error inesperado al crear el usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el usuario.");
        }
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getUser(@RequestHeader("Authorization") String tokenHeader) {

        String token = tokenHeader.replace("Bearer ", "");

        Long id = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));

        try{
            UserDTO userDTO = userService.getUserDTOById(id);
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            logger.error("Error al buscar el usuario com ID {}", id);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/me/resenas")
    public ResponseEntity<?> getMyReviews(@RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "");
        Long id = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));

        try {
            List<ResenaResponseDTO> reviews = resenaService.getUserReviews(id);
            return ResponseEntity.ok(reviews);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al cargar resenas del usuario con ID {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cargar tus resenas");
        }
    }

    @GetMapping("/image/{fileName}")
    public ResponseEntity<?> getImage(@PathVariable String fileName) {
        return fileStorageService.readImage(fileName);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping(value = "/me/image", consumes = "multipart/form-data")
    public ResponseEntity<UserDTO> uploadMyImage(
            @RequestParam("image") org.springframework.web.multipart.MultipartFile file,
            @RequestHeader("Authorization") String tokenHeader) {

        // 1. Extraer el token y el ID (Igual que en tu méto /me)
        String token = tokenHeader.replace("Bearer ", "");
        Long id = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));

        // Validamos que el ID no sea null antes de llamar al repositorio
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            logger.info("Subiendo imagen para el usuario con ID: {}", id);

            // 2. Llamamos al servicio pasando el ID extraído del token
            UserDTO updatedUser = userService.uploadImage(id, file);

            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("Error al actualizar imagen: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
