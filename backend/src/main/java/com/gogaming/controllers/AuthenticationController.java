package com.gogaming.controllers;

import com.gogaming.dtos.AuthRequestDTO;
import com.gogaming.dtos.AuthResponseDTO;
import com.gogaming.dtos.TwoFactorDTO;
import com.gogaming.entities.Role;
import com.gogaming.entities.User;
import com.gogaming.services.EmailService;
import com.gogaming.services.UserService;
import com.gogaming.utils.JwtUtil;
import com.gogaming.utils.RateLimiter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Login en dos pasos:
 * 1. POST /authenticate con email y contraseña → token pendiente de 2FA (5 min) y código de 6 cifras por email.
 * 2. POST /twofactor con ese token y el código → token completo (1 h).
 * Tras {@value #MAX_INTENTOS_2FA} códigos erróneos se invalida el código y hay que volver a empezar.
 */
@RestController
@RequestMapping("/api/v1")
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_INTENTOS_2FA = 5;

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final EmailService emailService;

    /**
     * Fuerza bruta de contraseñas: como mucho 10 fallos por IP y email, y 30 por IP, cada 15 minutos.
     * (Detrás de un proxy, getRemoteAddr es la IP del proxy: al desplegar hay que configurar forward-headers.)
     */
    private final RateLimiter fallosPorCuenta = new RateLimiter(10, Duration.ofMinutes(15));
    private final RateLimiter fallosPorIp = new RateLimiter(30, Duration.ofMinutes(15));

    /** Códigos erróneos por usuario desde el último código enviado. */
    private final Map<Long, Integer> intentosFallidos = new ConcurrentHashMap<>();

    public AuthenticationController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                                    UserService userService, EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponseDTO> authenticate(@Valid @RequestBody AuthRequestDTO authRequest, HttpServletRequest request) {
        if (authRequest.getEmail() == null || authRequest.getPassword() == null) {
            return respuesta(HttpStatus.BAD_REQUEST, "El correo electrónico y la contraseña son obligatorios.");
        }
        String ip = request.getRemoteAddr();
        String cuenta = ip + "|" + authRequest.getEmail().trim().toLowerCase(Locale.ROOT);
        if (fallosPorCuenta.bloqueado(cuenta) || fallosPorIp.bloqueado(ip)) {
            return respuesta(HttpStatus.TOO_MANY_REQUESTS, "Demasiados intentos fallidos. Espera unos minutos y vuelve a probar.");
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));

            String email = authentication.getName();
            Long id = userService.getIdByEmail(email);
            List<String> roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

            fallosPorCuenta.reiniciar(cuenta);
            String code = String.format("%06d", RANDOM.nextInt(1_000_000));
            userService.saveCode(code, id);
            intentosFallidos.remove(id);
            try {
                emailService.sendTwoFactorCodeEmail(email, code);
            } catch (Exception e) {
                logger.error("Error al enviar el correo de 2FA al usuario {}", id, e);
            }

            String token = jwtUtil.generateToken(email, roles, id, true);
            return ResponseEntity.ok(new AuthResponseDTO(token, "Authentication successful first part", true));

        } catch (BadCredentialsException e) {
            fallosPorCuenta.registrar(cuenta);
            fallosPorIp.registrar(ip);
            return respuesta(HttpStatus.UNAUTHORIZED, "Credenciales inválidas. Por favor, verifica tus datos.");
        } catch (DisabledException e) {
            return respuesta(HttpStatus.FORBIDDEN, "El correo electrónico no está verificado.");
        }
    }

    @PostMapping("/twofactor")
    public ResponseEntity<AuthResponseDTO> twofactor(@Valid @RequestBody TwoFactorDTO twoFactorDTO,
                                                     @RequestHeader(value = "Authorization", required = false) String tokenHeader) {
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            return respuesta(HttpStatus.UNAUTHORIZED, "Falta el token del primer paso.");
        }

        Claims claims;
        try {
            claims = jwtUtil.extractAllClaims(tokenHeader.substring(7));
        } catch (JwtException | IllegalArgumentException e) {
            return respuesta(HttpStatus.UNAUTHORIZED, "La sesión ha caducado. Vuelve a iniciar sesión.");
        }
        if (!jwtUtil.isPendingTwoFactor(claims)) {
            return respuesta(HttpStatus.BAD_REQUEST, "Este token no está pendiente de verificación.");
        }

        Long id = claims.get("id", Long.class);
        User user = id == null ? null : userService.getUserById(id);
        if (user == null || user.getCode() == null) {
            return respuesta(HttpStatus.UNAUTHORIZED, "El código ha caducado. Vuelve a iniciar sesión.");
        }

        if (!mismoCodigo(twoFactorDTO.getCode(), user.getCode())) {
            int intentos = intentosFallidos.merge(id, 1, Integer::sum);
            if (intentos >= MAX_INTENTOS_2FA) {
                userService.clear2FactorCode(id);
                intentosFallidos.remove(id);
                logger.warn("Código 2FA invalidado tras {} intentos fallidos (usuario {})", intentos, id);
                return respuesta(HttpStatus.UNAUTHORIZED, "Demasiados intentos. Vuelve a iniciar sesión para recibir un código nuevo.");
            }
            return respuesta(HttpStatus.UNAUTHORIZED, "El código enviado no es correcto.");
        }

        userService.clear2FactorCode(id);
        intentosFallidos.remove(id);
        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        String token = jwtUtil.generateToken(user.getEmail(), roles, id, false);
        return ResponseEntity.ok(new AuthResponseDTO(token, "Autenticación exitosa", false));
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<AuthResponseDTO> datosInvalidos(org.springframework.web.bind.MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(org.springframework.validation.FieldError::getDefaultMessage).findFirst().orElse("Datos no válidos.");
        return respuesta(HttpStatus.BAD_REQUEST, mensaje);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponseDTO> handleException(Exception e) {
        logger.error("Error inesperado en la autenticación", e);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado. Por favor, inténtalo de nuevo más tarde.");
    }

    /** Comparación en tiempo constante, para no dar pistas por el tiempo de respuesta. */
    private static boolean mismoCodigo(String recibido, String guardado) {
        return MessageDigest.isEqual(recibido.getBytes(StandardCharsets.UTF_8), guardado.getBytes(StandardCharsets.UTF_8));
    }

    private static ResponseEntity<AuthResponseDTO> respuesta(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(new AuthResponseDTO(null, mensaje, false));
    }
}
