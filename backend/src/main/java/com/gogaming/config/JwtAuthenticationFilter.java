package com.gogaming.config;

import com.gogaming.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.gogaming.services.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Autentica la petición con el JWT de la cabecera Authorization.
 *
 * - Un token no válido o caducado se ignora: la petición sigue como anónima. Así los endpoints
 *   públicos (el catálogo) funcionan aunque el navegador guarde un token viejo, y los protegidos
 *   responden 401 como siempre.
 * - Un token pendiente de 2FA no autentica nunca: solo lo acepta /api/v1/twofactor, que lo lee por su cuenta.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            autenticar(authHeader.substring(7), request);
        }
        filterChain.doFilter(request, response);
    }

    private void autenticar(String jwt, HttpServletRequest request) {
        Claims claims;
        try {
            claims = jwtUtil.extractAllClaims(jwt);
        } catch (JwtException | IllegalArgumentException e) {
            logger.debug("JWT ignorado ({}): {}", request.getRequestURI(), e.getMessage());
            return;
        }

        if (jwtUtil.isPendingTwoFactor(claims) || claims.getSubject() == null) {
            return;
        }

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(claims.getSubject());
        } catch (UsernameNotFoundException e) {
            return; // el usuario se ha borrado después de emitir el token
        }
        if (!userDetails.isEnabled()) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        List<SimpleGrantedAuthority> authorities = roles == null ? List.of()
                : roles.stream().map(SimpleGrantedAuthority::new).toList();

        var authToken = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
