package com.gogaming.controllers;

import jakarta.servlet.http.HttpServletRequest;
import com.gogaming.entities.User;
import com.gogaming.services.ChatService;
import com.gogaming.services.UserService;
import com.gogaming.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Chat del asistente de la tienda. Público (también para invitados) y con límite por IP. */
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public ChatController(ChatService chatService, JwtUtil jwtUtil, UserService userService) {
        this.chatService = chatService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    public record ChatRequest(List<ChatService.ChatMessage> messages) {}

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody ChatRequest request,
                                  @RequestHeader(value = "Authorization", required = false) String auth,
                                  HttpServletRequest http) {
        if (request == null || request.messages() == null || request.messages().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No hay mensajes"));
        }
        ChatService.ChatMessage last = request.messages().get(request.messages().size() - 1);
        if (last.content() == null || last.content().isBlank() || last.content().length() > ChatService.MAX_MESSAGE_CHARS) {
            return ResponseEntity.badRequest().body(Map.of("message", "El mensaje está vacío o es demasiado largo"));
        }
        if (!chatService.allow(http.getRemoteAddr())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Has enviado muchos mensajes seguidos. Espera un minuto, por favor."));
        }

        try {
            return ResponseEntity.ok(Map.of("reply", chatService.reply(request.messages(), firstNameFrom(auth))));
        } catch (Exception e) {
            logger.error("Error en el chat", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "El asistente no está disponible ahora mismo. Inténtalo en unos minutos."));
        }
    }

    /** Nombre del usuario si viene con sesión válida; si no, se le trata como invitado. */
    private String firstNameFrom(String auth) {
        try {
            if (auth == null || !auth.startsWith("Bearer ")) return null;
            Long id = jwtUtil.extractClaim(auth.substring(7), claims -> claims.get("id", Long.class));
            User user = id == null ? null : userService.getUserById(id);
            return user == null ? null : user.getFirstName();
        } catch (Exception e) {
            return null;
        }
    }
}
