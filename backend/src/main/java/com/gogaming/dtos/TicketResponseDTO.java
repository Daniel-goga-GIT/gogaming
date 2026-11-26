package com.gogaming.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDTO {
    private Long id;
    private Long userId;
    private String numeroTicket;
    private String fecha;
    private BigDecimal total;
    private String metodoPago;
    private String estado;
    private List<LineaTicketResponseDTO> lineas;
}