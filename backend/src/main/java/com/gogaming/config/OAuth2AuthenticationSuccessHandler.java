package com.gogaming.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.gogaming.entities.Role;
import com.gogaming.entities.User;
import com.gogaming.services.UserService;
import com.gogaming.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.util.List;

/**
 * Tras el login con Google: busca o crea el usuario y vuelve al frontend con un JWT completo
 * (Google ya hace de segundo factor, así que no se pide el código por email).
 */
@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.frontend.oauth2.redirect-uri}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        // Solo se acepta un email verificado por Google: si no, alguien podría entrar en una cuenta ajena.
        if (email == null || email.isBlank() || !Boolean.TRUE.equals(oAuth2User.getAttribute("email_verified"))) {
            response.sendRedirect(frontendRedirectUri + "#error=email");
            return;
        }

        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");

        User user = userService.findOrCreateGoogleUser(email, firstName, lastName);

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        String jwt = jwtUtil.generateToken(user.getEmail(), roles, user.getId(), false);

        // El token va en el fragmento (#token=...): el navegador no lo envía a ningún servidor,
        // así que no queda en logs ni en cabeceras Referer.
        response.sendRedirect(frontendRedirectUri + "#token=" + UriUtils.encode(jwt, StandardCharsets.UTF_8));
    }
}
