package org.iesalixar.daw2.alejandromontero.ticket_logger_api.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Role;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.User;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.services.UserService;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

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
        if (email == null || email.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No se pudo obtener el email de Google");
            return;
        }

        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");

        User user = userService.findOrCreateGoogleUser(email, firstName, lastName);

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        String jwt = jwtUtil.generateToken(user.getEmail(), roles, user.getId(), false);

        String targetUrl = UriComponentsBuilder
                .fromUriString(frontendRedirectUri)
                .queryParam("token", jwt)
                .build()
                .toUriString();

        response.sendRedirect(targetUrl);
    }
}
