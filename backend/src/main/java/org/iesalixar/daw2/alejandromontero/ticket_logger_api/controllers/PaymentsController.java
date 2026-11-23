package org.iesalixar.daw2.alejandromontero.ticket_logger_api.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.CreateCheckoutRequestDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.TicketResponseDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.User;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.services.EmailService;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.services.TicketService;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.services.UserService;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentsController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentsController.class);

    private final TicketService ticketService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final EmailService emailService;

    public PaymentsController(TicketService ticketService, JwtUtil jwtUtil, UserService userService, EmailService emailService) {
        this.ticketService = ticketService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.emailService = emailService;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/checkout-session")
    public ResponseEntity<?> completeCheckout(
            @RequestHeader("Authorization") String tokenHeader,
            @Valid @RequestBody CreateCheckoutRequestDTO request
    ) {
        try {
            Long userId = extractUserId(tokenHeader);
            TicketResponseDTO createdTicket = ticketService.createTicketFromCheckout(userId, request.getItems(), request.getMetodoPago());

            try {
                User user = userService.getUserById(userId);
                emailService.sendPaymentConfirmationEmail(user.getEmail(), user.getFirstName(), createdTicket);
            } catch (Exception mailException) {
                logger.error("No se pudo enviar el correo de confirmacion del pago para ticket {}", createdTicket.getNumeroTicket(), mailException);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(createdTicket);
        } catch (IllegalArgumentException e) {
            logger.warn("Checkout rechazado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al completar checkout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al completar la compra");
        }
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/tickets")
    public ResponseEntity<?> getMyTickets(@RequestHeader("Authorization") String tokenHeader) {
        try {
            Long userId = extractUserId(tokenHeader);
            List<TicketResponseDTO> tickets = ticketService.getTicketsByUserId(userId);
            return ResponseEntity.ok(tickets);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al cargar tickets del usuario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cargar historial de pedidos");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/tickets/all")
    public ResponseEntity<?> getAllTickets() {
        try {
            return ResponseEntity.ok(ticketService.getAllTickets());
        } catch (Exception e) {
            logger.error("Error al cargar todos los pedidos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cargar los pedidos");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/tickets/{id}/estado")
    public ResponseEntity<?> updateEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String estado = body.get("estado");
            if (estado == null || estado.isBlank()) {
                return ResponseEntity.badRequest().body("El campo 'estado' es requerido");
            }
            return ResponseEntity.ok(ticketService.updateEstado(id, estado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al actualizar estado del pedido {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar el estado");
        }
    }

    private Long extractUserId(String tokenHeader) {
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token de autorizacion invalido");
        }

        String token = tokenHeader.replace("Bearer ", "");
        Long userId = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));

        if (userId == null) {
            throw new IllegalArgumentException("No se pudo obtener el usuario autenticado");
        }

        return userId;
    }
}
