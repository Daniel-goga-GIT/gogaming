package com.gogaming.services;

import com.stripe.StripeClient;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;
import com.gogaming.dtos.CartItemRequestDTO;
import com.gogaming.dtos.TicketResponseDTO;
import com.gogaming.entities.Producto;
import com.gogaming.entities.User;
import com.gogaming.repositories.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Pagos con Stripe Checkout.
 *
 * Flujo:
 *  1. {@link #createCheckoutSession}: valida la cesta contra la BD (precios y stock reales,
 *     nunca los del navegador) y crea una Checkout Session. El cliente paga en la página de Stripe.
 *  2. {@link #fulfill}: cuando Stripe confirma el pago (webhook o vuelta del cliente a la web),
 *     se crea el pedido y se descuenta el stock. Es idempotente: un pago = un pedido.
 */
@Service
public class StripePaymentService {

    private static final Logger logger = LoggerFactory.getLogger(StripePaymentService.class);
    private static final int MAX_LINES = 25;
    private static final int MAX_QUANTITY_PER_LINE = 10;
    private static final String META_USER = "userId";
    private static final String META_CART = "cart";
    private static final java.util.regex.Pattern CHECKOUT_SESSION_ID =
            java.util.regex.Pattern.compile("\"id\"\\s*:\\s*\"(cs_(?:test|live)_[A-Za-z0-9]+)\"");

    private final ProductoRepository productoRepository;
    private final TicketService ticketService;
    private final UserService userService;
    private final EmailService emailService;

    @Value("${STRIPE_SECRET_KEY:}")
    private String secretKey;

    @Value("${STRIPE_WEBHOOK_SECRET:}")
    private String webhookSecret;

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    private StripeClient client;

    public StripePaymentService(ProductoRepository productoRepository, TicketService ticketService,
                                UserService userService, EmailService emailService) {
        this.productoRepository = productoRepository;
        this.ticketService = ticketService;
        this.userService = userService;
        this.emailService = emailService;
    }

    private StripeClient client() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("Falta STRIPE_SECRET_KEY en la configuración");
        }
        if (client == null) {
            client = new StripeClient(secretKey);
        }
        return client;
    }

    /** Crea la Checkout Session y devuelve su id y la URL de pago de Stripe. */
    public Map<String, String> createCheckoutSession(Long userId, List<CartItemRequestDTO> items) throws StripeException {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("La cesta está vacía");
        }

        // Agrupa líneas repetidas del mismo producto.
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (CartItemRequestDTO item : items) {
            if (item.getProductId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Hay un producto no válido en la cesta");
            }
            quantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }
        if (quantities.size() > MAX_LINES) {
            throw new IllegalArgumentException("La cesta tiene demasiados productos distintos (máximo " + MAX_LINES + ")");
        }

        User user = userService.getUserById(userId);
        SessionCreateParams.Builder params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setLocale(SessionCreateParams.Locale.ES)
                .setSuccessUrl(frontendBaseUrl + "/pago-completado?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendBaseUrl + "/cesta?pago=cancelado")
                .setClientReferenceId(String.valueOf(userId))
                .putMetadata(META_USER, String.valueOf(userId));
        if (user != null && user.getEmail() != null) {
            params.setCustomerEmail(user.getEmail());
        }

        // Formato compacto en metadata: "id x cantidad @ céntimos;..." (precio que se cobra).
        StringBuilder cart = new StringBuilder();
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Producto product = productoRepository.findById(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Uno de los productos ya no existe"));
            int qty = entry.getValue();
            if (qty > MAX_QUANTITY_PER_LINE) {
                throw new IllegalArgumentException("Máximo " + MAX_QUANTITY_PER_LINE + " unidades de " + product.getNombre());
            }
            int stock = product.getStock() == null ? 0 : product.getStock();
            if (stock < qty) {
                throw new IllegalArgumentException(stock == 0
                        ? product.getNombre() + " está agotado"
                        : "Solo quedan " + stock + " unidades de " + product.getNombre());
            }

            long unitCents = product.getPrecio().movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
            SessionCreateParams.LineItem.PriceData.ProductData.Builder productData =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName((product.getMarca() != null ? product.getMarca() + " " : "") + product.getNombre())
                            .putMetadata("productId", String.valueOf(product.getId()));
            if (product.getImage() != null && product.getImage().startsWith("https://")) {
                productData.addImage(product.getImage());
            }

            params.addLineItem(SessionCreateParams.LineItem.builder()
                    .setQuantity((long) qty)
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("eur")
                            .setUnitAmount(unitCents)
                            .setProductData(productData.build())
                            .build())
                    .build());

            if (!cart.isEmpty()) cart.append(';');
            cart.append(product.getId()).append('x').append(qty).append('@').append(unitCents);
        }
        params.putMetadata(META_CART, cart.toString());

        Session session = client().v1().checkout().sessions().create(params.build());
        logger.info("Checkout Session {} creada para el usuario {}", session.getId(), userId);
        return Map.of("sessionId", session.getId(), "url", session.getUrl());
    }

    /**
     * Comprueba en Stripe que la sesión está pagada y crea el pedido si aún no existe.
     * Devuelve vacío si el pago todavía no se ha completado.
     */
    public Optional<TicketResponseDTO> fulfill(String sessionId) throws StripeException {
        Optional<TicketResponseDTO> existing = ticketService.findByStripeSessionId(sessionId);
        if (existing.isPresent()) {
            return existing;
        }

        Session session = client().v1().checkout().sessions().retrieve(sessionId, SessionRetrieveParams.builder().build());
        if (!"paid".equals(session.getPaymentStatus())) {
            return Optional.empty();
        }

        Long userId = Long.valueOf(session.getMetadata().get(META_USER));
        List<TicketService.PaidLine> lines = parseCart(session.getMetadata().get(META_CART));

        TicketResponseDTO ticket;
        try {
            ticket = ticketService.createPaidTicket(userId, lines, sessionId);
        } catch (DataIntegrityViolationException duplicate) {
            // Otra petición (webhook o navegador) creó el pedido a la vez: devolvemos ese.
            return ticketService.findByStripeSessionId(sessionId);
        }

        logger.info("Pedido {} creado para la sesión de Stripe {}", ticket.getNumeroTicket(), sessionId);
        sendConfirmationEmail(userId, ticket);
        return Optional.of(ticket);
    }

    /** Valida la firma del webhook y procesa los pagos completados. */
    public void handleWebhook(String payload, String signatureHeader) throws SignatureVerificationException, StripeException {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalStateException("Falta STRIPE_WEBHOOK_SECRET en la configuración");
        }
        Event event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);

        switch (event.getType()) {
            case "checkout.session.completed", "checkout.session.async_payment_succeeded" -> {
                String sessionId = extractObjectId(event);
                if (sessionId != null) {
                    fulfill(sessionId);
                }
            }
            default -> logger.debug("Evento de Stripe ignorado: {}", event.getType());
        }
    }

    /** Id de la sesión del evento, leído del JSON crudo (robusto ante versiones de API distintas). */
    private String extractObjectId(Event event) {
        String raw = event.getDataObjectDeserializer().getRawJson();
        if (raw == null) return null;
        var matcher = CHECKOUT_SESSION_ID.matcher(raw);
        return matcher.find() ? matcher.group(1) : null;
    }

    private List<TicketService.PaidLine> parseCart(String cart) {
        if (cart == null || cart.isBlank()) {
            throw new IllegalStateException("La sesión de Stripe no tiene productos en metadata");
        }
        List<TicketService.PaidLine> lines = new ArrayList<>();
        for (String part : cart.split(";")) {
            String[] idAndRest = part.split("x", 2);
            String[] qtyAndPrice = idAndRest[1].split("@", 2);
            lines.add(new TicketService.PaidLine(
                    Long.valueOf(idAndRest[0]),
                    Integer.parseInt(qtyAndPrice[0]),
                    BigDecimal.valueOf(Long.parseLong(qtyAndPrice[1]), 2)));
        }
        return lines;
    }

    private void sendConfirmationEmail(Long userId, TicketResponseDTO ticket) {
        try {
            User user = userService.getUserById(userId);
            if (user != null && user.getEmail() != null) {
                emailService.sendPaymentConfirmationEmail(user.getEmail(), user.getFirstName(), ticket);
            }
        } catch (Exception e) {
            logger.error("No se pudo enviar el correo de confirmación del pedido {}", ticket.getNumeroTicket(), e);
        }
    }
}
