package com.gogaming.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineaTicketResponseDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}