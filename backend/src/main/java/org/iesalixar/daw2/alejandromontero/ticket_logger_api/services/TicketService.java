package org.iesalixar.daw2.alejandromontero.ticket_logger_api.services;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.LineaTicketResponseDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.TicketResponseDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.LineaTicket;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Producto;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Ticket;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories.LineaTicketRepository;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories.ProductoRepository;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final LineaTicketRepository lineaTicketRepository;
    private final ProductoRepository productoRepository;

    public TicketService(TicketRepository ticketRepository, LineaTicketRepository lineaTicketRepository, ProductoRepository productoRepository) {
        this.ticketRepository = ticketRepository;
        this.lineaTicketRepository = lineaTicketRepository;
        this.productoRepository = productoRepository;
    }

    /** Línea pagada: producto, cantidad y precio unitario cobrado por Stripe. */
    public record PaidLine(Long productId, int quantity, BigDecimal unitPrice) {}

    public java.util.Optional<TicketResponseDTO> findByStripeSessionId(String sessionId) {
        return ticketRepository.findByStripeSessionId(sessionId)
                .map(t -> buildTicketResponse(t, lineaTicketRepository.findByTicketId(t.getId())));
    }

    /**
     * Crea el pedido de una compra YA PAGADA en Stripe y descuenta el stock.
     * Es transaccional: o se guarda todo o nada. La columna única stripe_session_id
     * impide crear dos pedidos para el mismo pago (webhook + vuelta del cliente a la vez).
     */
    @Transactional
    public TicketResponseDTO createPaidTicket(Long userId, List<PaidLine> paidLines, String stripeSessionId) {
        if (paidLines == null || paidLines.isEmpty()) {
            throw new IllegalArgumentException("El pago no contiene productos");
        }

        List<LineaTicket> lineas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (PaidLine paid : paidLines) {
            Producto product = productoRepository.findById(paid.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + paid.productId()));

            int stock = product.getStock() == null ? 0 : product.getStock();
            if (stock < paid.quantity()) {
                // Ya está cobrado: se registra igualmente y se deja el stock a 0 para revisión manual.
                logger.warn("Stock insuficiente tras el pago {} (producto {}, stock {}, pedido {})",
                        stripeSessionId, product.getId(), stock, paid.quantity());
            }
            product.setStock(Math.max(0, stock - paid.quantity()));
            productoRepository.save(product);

            LineaTicket linea = new LineaTicket();
            linea.setProductId(product.getId());
            linea.setCantidad(paid.quantity());
            linea.setPrecioUnitario(paid.unitPrice());
            lineas.add(linea);
            total = total.add(paid.unitPrice().multiply(BigDecimal.valueOf(paid.quantity())));
        }

        Ticket ticket = new Ticket();
        ticket.setUserId(userId);
        ticket.setNumeroTicket(generateTicketNumber());
        ticket.setFecha(LocalDateTime.now());
        ticket.setTotal(total);
        ticket.setMetodoPago("Tarjeta");
        ticket.setEstado("Preparando");
        ticket.setStripeSessionId(stripeSessionId);
        Ticket savedTicket = ticketRepository.saveAndFlush(ticket);

        lineas.forEach(linea -> linea.setTicketId(savedTicket.getId()));
        lineaTicketRepository.saveAll(lineas);

        return buildTicketResponse(savedTicket, lineas);
    }

    public List<TicketResponseDTO> getTicketsByUserId(Long userId) {
        List<Ticket> tickets = ticketRepository.findByUserId(userId);

        return tickets.stream()
                .map(ticket -> buildTicketResponse(ticket, lineaTicketRepository.findByTicketId(ticket.getId())))
                .collect(Collectors.toList());
    }

    public List<TicketResponseDTO> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(ticket -> buildTicketResponse(ticket, lineaTicketRepository.findByTicketId(ticket.getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public TicketResponseDTO updateEstado(Long ticketId, String estado) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + ticketId));
        ticket.setEstado(estado);
        Ticket saved = ticketRepository.save(ticket);
        return buildTicketResponse(saved, lineaTicketRepository.findByTicketId(saved.getId()));
    }

    private TicketResponseDTO buildTicketResponse(Ticket ticket, List<LineaTicket> lineas) {
        List<LineaTicketResponseDTO> lineasResponse = lineas.stream().map(linea -> {
            Producto product = productoRepository.findById(linea.getProductId()).orElse(null);
            String productName = product != null ? product.getNombre() : "Producto desconocido";
            String productImage = product != null ? product.getImage() : null;

            BigDecimal subtotal = linea.getPrecioUnitario().multiply(new BigDecimal(linea.getCantidad()));

            return new LineaTicketResponseDTO(
                    linea.getId(),
                    linea.getProductId(),
                    productName,
                productImage,
                    linea.getCantidad(),
                    linea.getPrecioUnitario(),
                    subtotal
            );
        }).collect(Collectors.toList());

        String fechaStr = ticket.getFecha() != null
                ? ticket.getFecha().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null;

        return new TicketResponseDTO(
                ticket.getId(),
                ticket.getUserId(),
                ticket.getNumeroTicket(),
                fechaStr,
                ticket.getTotal(),
                ticket.getMetodoPago(),
                ticket.getEstado(),
                lineasResponse
        );
    }

    private static final String TICKET_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final java.security.SecureRandom RANDOM = new java.security.SecureRandom();

    /** Número de pedido legible y único, p. ej. GG-261123-7KQ2M (cabe en VARCHAR(20)). */
    private String generateTicketNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        StringBuilder suffix = new StringBuilder(5);
        for (int i = 0; i < 5; i++) {
            suffix.append(TICKET_ALPHABET.charAt(RANDOM.nextInt(TICKET_ALPHABET.length())));
        }
        return "GG-" + date + "-" + suffix;
    }
}
