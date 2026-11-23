package org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idticket")
    private Long id;

    @Column(name = "idusuario")
    private Long userId;

    @Column(name = "numero_ticket", nullable = false, unique = true)
    private String numeroTicket;

    @CreationTimestamp
    @Column(name = "fecha", updatable = false)
    private LocalDateTime fecha;

    @Column(name = "total", nullable = false)
    private BigDecimal total;

    @Column(name = "metodo_pago", nullable = false)
    private String metodoPago;

    @Column(name = "estado", nullable = false)
    private String estado;

    /** Id de la Checkout Session de Stripe que pagó este pedido (evita crear el pedido dos veces). */
    @Column(name = "stripe_session_id", unique = true)
    private String stripeSessionId;
}
