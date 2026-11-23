package org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auriculares")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "idproducto")
public class Auriculares extends Producto {

    private String tipo;

    private String driver;

    private String conectividad;

    private String impedancia;
}
