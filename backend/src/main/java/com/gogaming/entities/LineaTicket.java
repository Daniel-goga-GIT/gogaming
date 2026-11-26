package com.gogaming.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "lineas_ticket")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineaTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idlinea")
    private Long id;

    @Column(name = "idticket", nullable = false)
    private Long ticketId;

    @Column(name = "idproducto", nullable = false)
    private Long productId;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private BigDecimal precioUnitario;
}
