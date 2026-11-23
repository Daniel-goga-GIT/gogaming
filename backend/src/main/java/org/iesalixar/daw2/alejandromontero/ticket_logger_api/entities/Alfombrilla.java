package org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "alfombrilla")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "idproducto")
public class Alfombrilla extends Producto {

    private String dimensiones;

    private BigDecimal grosor;

    private String superficie;
}
