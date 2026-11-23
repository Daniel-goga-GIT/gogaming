package org.iesalixar.daw2.alejandromontero.ticket_logger_api.services;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.CartItemRequestDTO;
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
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final LineaTicketRepository lineaTicketRepository;
    private final ProductoRepository productoRepository;

    public TicketService(TicketRepository ticketRepository, LineaTicketRepository lineaTicketRepository, ProductoRepository productoRepository) {
        this.ticketRepository = ticketRepository;
        this.lineaTicketRepository = lineaTicketRepository;
        this.productoRepository = productoRepository;
    }

    public TicketResponseDTO createTicketFromCheckout(Long userId, List<CartItemRequestDTO> items, String metodoPago) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("La cesta esta vacia");
        }

        String paymentMethod = normalizePaymentMethod(metodoPago);

        List<LineaTicket> lineas = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItemRequestDTO item : items) {
            if (item.getProductId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Item de cesta invalido");
            }

            Producto product = productoRepository.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + item.getProductId()));

            if (product.getStock() == null || product.getStock() < item.getQuantity()) {
                throw new IllegalArgumentException("Stock insuficiente para el producto " + product.getNombre());
            }

            product.setStock(product.getStock() - item.getQuantity());
            productoRepository.save(product);

            BigDecimal lineSubtotal = product.getPrecio().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(lineSubtotal);

            LineaTicket linea = new LineaTicket();
            linea.setProductId(product.getId());
            linea.setCantidad(item.getQuantity());
            linea.setPrecioUnitario(product.getPrecio());
            lineas.add(linea);
        }

        BigDecimal total = subtotal;

        Ticket ticket = new Ticket();
        ticket.setUserId(userId);
        ticket.setNumeroTicket(generateTicketNumber());
        ticket.setFecha(LocalDateTime.now());
        ticket.setTotal(total);
        ticket.setMetodoPago(paymentMethod);
        ticket.setEstado("Preparando");

        Ticket savedTicket = ticketRepository.save(ticket);

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

    private String generateTicketNumber() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "Ticket-" + LocalDateTime.now().format(formatter);
    }

    private String normalizePaymentMethod(String rawMethod) {
        if (rawMethod == null || rawMethod.isBlank()) {
            return "Tarjeta";
        }

        String normalized = rawMethod.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "tarjeta" -> "Tarjeta";
            case "paypal" -> "PayPal";
            case "transferencia" -> "Transferencia";
            default -> throw new IllegalArgumentException("Metodo de pago no valido: " + rawMethod);
        };
    }
}
