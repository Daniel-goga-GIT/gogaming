package com.gogaming.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Autowired
    private KeyPair jwtKeyPair;
    private static final long JWT_EXPIRATION = 3600000;

    private static final long JWT_EXPIRATION_TWOFACTOR = 60000;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.max.expiration.seconds}")
    private int maxExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }


    public String getUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }


    public Claims extractAllClaims(String token) {
        return Jwts.parser() // JwtParserBuilder
                .verifyWith(jwtKeyPair.getPublic()) // Configura la clave para verificar la firma
                .build() // JwtParser
                .parseSignedClaims(token) // Verifica el token y lo parsea
                .getPayload(); // Devuelve el cuerpo del JWT (claims)
    }

    public String generateToken(String username, List<String> roles, Long id, boolean twofactor) {
        return Jwts.builder()
                .subject(username) // Configura el claim "sub" (nombre de usuario)
                .claim("roles", roles)
                .claim("id" , id) // Incluye los roles como claim adicional
                .claim("twofactor", twofactor)
                .issuedAt(new Date()) // Fecha de emisión del token
                .expiration(new Date(System.currentTimeMillis() + (twofactor ? JWT_EXPIRATION_TWOFACTOR : JWT_EXPIRATION))) // Expira en 1 hora o 5 minutos
                .signWith(jwtKeyPair.getPrivate(), Jwts.SIG.RS256) // Firma el token con la clave secreta
                .compact(); // Genera el token en formato JWT
    }

    public boolean validateToken(String token, String username) {
        Claims claims = Jwts.parser()
                .verifyWith(jwtKeyPair.getPublic())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return username.equals(claims.getSubject()) && !isTokenExpired(claims);
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}