package org.iesalixar.daw2.alejandromontero.ticket_logger_api.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.CreateCheckoutRequestDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.TicketResponseDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.services.StripePaymentService;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.services.TicketService;
import com.stripe.exception.SignatureVerificationException;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentsController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentsController.class);

    private final TicketService ticketService;
    private final JwtUtil jwtUtil;
    private final StripePaymentService stripePaymentService;

    public PaymentsController(TicketService ticketService, JwtUtil jwtUtil, StripePaymentService stripePaymentService) {
        this.ticketService = ticketService;
        this.jwtUtil = jwtUtil;
        this.stripePaymentService = stripePaymentService;
    }

    /** Crea la sesión de pago de Stripe con los precios de la BD y devuelve la URL a la que redirigir. */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/checkout-session")
    public ResponseEntity<?> createCheckoutSession(
            @RequestHeader("Authorization") String tokenHeader,
            @Valid @RequestBody CreateCheckoutRequestDTO request
    ) {
        try {
            Long userId = extractUserId(tokenHeader);
            return ResponseEntity.ok(stripePaymentService.createCheckoutSession(userId, request.getItems()));
        } catch (IllegalArgumentException e) {
            logger.warn("Checkout rechazado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error al crear la sesión de pago", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", "No se ha podido iniciar el pago. Inténtalo de nuevo."));
        }
    }

    /**
     * El cliente vuelve de Stripe: confirma el pago y devuelve el pedido.
     * 200 = pedido creado, 202 = Stripe aún no lo marca como pagado, 403 = la sesión es de otro usuario.
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/checkout-session/{sessionId}")
    public ResponseEntity<?> confirmCheckoutSession(
            @RequestHeader("Authorization") String tokenHeader,
            @PathVariable String sessionId
    ) {
        try {
            if (!sessionId.matches("cs_(test|live)_[A-Za-z0-9]+")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Sesión de pago no válida"));
            }
            Long userId = extractUserId(tokenHeader);
            Optional<TicketResponseDTO> ticket = stripePaymentService.fulfill(sessionId);
            if (ticket.isEmpty()) {
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("status", "pending"));
            }
            if (!userId.equals(ticket.get().getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Este pedido no es tuyo"));
            }
            return ResponseEntity.ok(ticket.get());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (com.stripe.exception.InvalidRequestException e) {
            if (Integer.valueOf(404).equals(e.getStatusCode())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Sesión de pago no encontrada"));
            }
            logger.error("Stripe rechazó la consulta de la sesión {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", "No se ha podido confirmar el pago"));
        } catch (Exception e) {
            logger.error("Error al confirmar la sesión de pago {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", "No se ha podido confirmar el pago"));
        }
    }

    /** Webhook de Stripe (público, protegido por la firma Stripe-Signature). */
    @PostMapping("/webhook")
    public ResponseEntity<String> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signature
    ) {
        if (signature == null) {
            return ResponseEntity.badRequest().body("Falta la firma");
        }
        try {
            stripePaymentService.handleWebhook(payload, signature);
            return ResponseEntity.ok("ok");
        } catch (SignatureVerificationException e) {
            logger.warn("Webhook de Stripe con firma no válida");
            return ResponseEntity.badRequest().body("Firma no válida");
        } catch (Exception e) {
            // 500 => Stripe reintentará el envío más tarde.
            logger.error("Error procesando webhook de Stripe", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
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
