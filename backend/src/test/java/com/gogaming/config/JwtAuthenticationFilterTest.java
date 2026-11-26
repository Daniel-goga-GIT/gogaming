package com.gogaming.config;

import com.gogaming.TestKeys;
import com.gogaming.services.CustomUserDetailsService;
import com.gogaming.utils.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private final JwtUtil jwtUtil = new JwtUtil(TestKeys.keyPair());
    private final CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);

    @BeforeEach
    void usuarioExiste() {
        when(userDetailsService.loadUserByUsername("ana@example.com")).thenReturn(
                User.withUsername("ana@example.com").password("x").authorities("ROLE_ADMIN").build());
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private Authentication filtrar(String authorization) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/me");
        if (authorization != null) {
            request.addHeader("Authorization", authorization);
        }
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        assertThat(chain.getRequest()).as("la petición siempre continúa").isNotNull();
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Test
    void unTokenCompletoAutenticaConSusRoles() throws Exception {
        String token = jwtUtil.generateToken("ana@example.com", List.of("ROLE_ADMIN"), 1L, false);

        Authentication auth = filtrar("Bearer " + token);

        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("ana@example.com");
        assertThat(auth.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_ADMIN");
    }

    /** Regresión: el filtro buscaba el claim "twoFactor" y el token lleva "twofactor", así que el 2FA no servía de nada. */
    @Test
    void unTokenPendienteDe2faNoAutentica() throws Exception {
        String pendiente = jwtUtil.generateToken("ana@example.com", List.of("ROLE_ADMIN"), 1L, true);

        assertThat(filtrar("Bearer " + pendiente)).isNull();
    }

    @Test
    void unTokenInvalidoSeIgnoraYLaPeticionSigueComoAnonima() throws Exception {
        assertThat(filtrar("Bearer no.es.un.jwt")).isNull();
    }

    @Test
    void sinCabeceraNoHaceNada() throws Exception {
        assertThat(filtrar(null)).isNull();
    }

    @Test
    void siElUsuarioYaNoExisteNoAutentica() throws Exception {
        when(userDetailsService.loadUserByUsername(anyString())).thenThrow(new UsernameNotFoundException("borrado"));
        String token = jwtUtil.generateToken("borrado@example.com", List.of("ROLE_USER"), 2L, false);

        assertThat(filtrar("Bearer " + token)).isNull();
    }
}
