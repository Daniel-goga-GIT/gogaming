package com.gogaming.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "raton")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true) // Importante para que Lombok considere los campos del padre
@PrimaryKeyJoinColumn(name = "idproducto") // Indica que la FK es la PK de esta tabla
public class Raton extends Producto {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_agarre")
    private TipoAgarre tipoAgarre;

    private String medidas;

    private String sensor;

    private BigDecimal peso;

    private String switches;
}


