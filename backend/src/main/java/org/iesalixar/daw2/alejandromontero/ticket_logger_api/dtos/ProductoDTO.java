package org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.TipoProducto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Campos comunes a todos los productos. Cada categoría añade los suyos en una subclase
 * ({@link RatonDTO}, {@link TecladoDTO}...). La validación se declara con anotaciones.
 */
@Data
@NoArgsConstructor
public abstract class ProductoDTO {

    private Long id;

    @NotBlank(message = "Nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "Marca es obligatoria")
    private String marca;

    private LocalDate fechaPublicacion;

    @NotNull(message = "Precio es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    private BigDecimal precio;

    @NotNull(message = "Stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stock;

    /** Lo fija el servidor según la categoría de la URL; lo que envíe el cliente se ignora. */
    private TipoProducto tipoProducto;

    @NotBlank(message = "Imagen es obligatoria")
    private String image;
}
