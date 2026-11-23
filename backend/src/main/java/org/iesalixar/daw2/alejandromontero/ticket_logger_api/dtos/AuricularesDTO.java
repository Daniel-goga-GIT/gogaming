package org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.TipoProducto;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuricularesDTO {

    private Long id;
    private String nombre;
    private String marca;
    private LocalDate fechaPublicacion;
    private BigDecimal precio;
    private Integer stock;
    private TipoProducto tipoProducto;
    private String image;
    private String tipo;
    private String driver;
    private String conectividad;
    private String impedancia;
}
