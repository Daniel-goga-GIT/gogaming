package com.gogaming.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * Emite y lee los JWT (RS256, con la clave del keystore {@code jwt.jks}).
 * Hay dos tipos de token:
 * - Pendiente de 2FA ({@code twofactor = true}): se emite tras comprobar la contraseña y
 *   solo sirve para llamar a /api/v1/twofactor.
 * - Completo ({@code twofactor = false}): da acceso según los roles.
 */
@Component
public class JwtUtil {

    /** Nombre del claim que marca un token pendiente de 2FA. */
    public static final String CLAIM_TWOFACTOR = "twofactor";

    private static final long JWT_EXPIRATION_MS = 60 * 60 * 1000;          // 1 hora
    private static final long JWT_EXPIRATION_TWOFACTOR_MS = 5 * 60 * 1000; // 5 minutos para leer el email

    private final KeyPair jwtKeyPair;

    public JwtUtil(KeyPair jwtKeyPair) {
        this.jwtKeyPair = jwtKeyPair;
    }

    public String getUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    /**
     * Verifica la firma y la caducidad y devuelve los claims.
     * Lanza {@link io.jsonwebtoken.JwtException} si el token no es válido o ha caducado.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtKeyPair.getPublic())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isPendingTwoFactor(Claims claims) {
        return Boolean.TRUE.equals(claims.get(CLAIM_TWOFACTOR, Boolean.class));
    }

    public String generateToken(String username, List<String> roles, Long id, boolean pendingTwoFactor) {
        long duracion = pendingTwoFactor ? JWT_EXPIRATION_TWOFACTOR_MS : JWT_EXPIRATION_MS;
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .claim("id", id)
                .claim(CLAIM_TWOFACTOR, pendingTwoFactor)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + duracion))
                .signWith(jwtKeyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
    }
}
