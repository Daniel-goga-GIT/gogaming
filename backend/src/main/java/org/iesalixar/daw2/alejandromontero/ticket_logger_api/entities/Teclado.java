package org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "teclado")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "idproducto")
public class Teclado extends Producto {

    @Column(name = "tipo_switch")
    private String tipoSwitch;

    private String layout;

    private String conectividad;

    private String retroiluminacion;
}
