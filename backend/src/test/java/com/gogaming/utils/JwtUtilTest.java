package com.gogaming.utils;

import com.gogaming.TestKeys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import java.security.KeyPairGenerator;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil(TestKeys.keyPair());

    @Test
    void elTokenCompletoLlevaUsuarioRolesEIdYNoEstaPendiente() {
        String token = jwtUtil.generateToken("ana@example.com", List.of("ROLE_USER"), 7L, false);

        Claims claims = jwtUtil.extractAllClaims(token);
        assertThat(claims.getSubject()).isEqualTo("ana@example.com");
        assertThat(claims.get("roles", List.class)).containsExactly("ROLE_USER");
        assertThat(claims.get("id", Long.class)).isEqualTo(7L);
        assertThat(jwtUtil.isPendingTwoFactor(claims)).isFalse();
    }

    @Test
    void elTokenDelPrimerPasoEstaPendienteDe2faYDuraCincoMinutos() {
        long antes = System.currentTimeMillis();
        Claims claims = jwtUtil.extractAllClaims(jwtUtil.generateToken("ana@example.com", List.of("ROLE_USER"), 7L, true));

        assertThat(jwtUtil.isPendingTwoFactor(claims)).isTrue();
        long duracion = claims.getExpiration().getTime() - antes;
        assertThat(duracion).isBetween(4 * 60_000L, 5 * 60_000L + 1_000);
    }

    @Test
    void rechazaUnTokenFirmadoConOtraClave() throws Exception {
        var otraClave = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        String ajeno = Jwts.builder().subject("intruso@example.com")
                .claim("roles", List.of("ROLE_ADMIN"))
                .signWith(otraClave.getPrivate(), Jwts.SIG.RS256).compact();

        assertThatThrownBy(() -> jwtUtil.extractAllClaims(ajeno)).isInstanceOf(JwtException.class);
    }

    @Test
    void rechazaUnTokenCaducado() {
        String caducado = Jwts.builder().subject("ana@example.com")
                .expiration(new Date(System.currentTimeMillis() - 1_000))
                .signWith(TestKeys.keyPair().getPrivate(), Jwts.SIG.RS256).compact();

        assertThatThrownBy(() -> jwtUtil.extractAllClaims(caducado)).isInstanceOf(JwtException.class);
    }
}
