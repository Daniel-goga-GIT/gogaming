package com.gogaming.services;

import com.gogaming.dtos.*;
import com.gogaming.entities.*;

import java.util.Arrays;
import java.util.Optional;

/**
 * Las categorías del catálogo: une el segmento de la URL ({@code /api/v1/productos/raton}),
 * el valor de {@link TipoProducto}, la entidad JPA y el DTO de cada una.
 * Para añadir una categoría basta con su entidad, su DTO y una línea aquí.
 */
public enum CategoriaProducto {

    RATON("raton", TipoProducto.Raton, Raton.class, RatonDTO.class),
    TECLADO("teclado", TipoProducto.Teclado, Teclado.class, TecladoDTO.class),
    SKATE("skate", TipoProducto.Skate, Skate.class, SkateDTO.class),
    ALFOMBRILLA("alfombrilla", TipoProducto.Alfombrilla, Alfombrilla.class, AlfombrillaDTO.class),
    AURICULARES("auriculares", TipoProducto.Auriculares, Auriculares.class, AuricularesDTO.class),
    MANGA("manga", TipoProducto.Manga, Manga.class, MangaDTO.class);

    private final String slug;
    private final TipoProducto tipo;
    private final Class<? extends Producto> entidad;
    private final Class<? extends ProductoDTO> dto;

    CategoriaProducto(String slug, TipoProducto tipo, Class<? extends Producto> entidad, Class<? extends ProductoDTO> dto) {
        this.slug = slug;
        this.tipo = tipo;
        this.entidad = entidad;
        this.dto = dto;
    }

    public String getSlug() { return slug; }
    public TipoProducto getTipo() { return tipo; }
    public Class<? extends Producto> getEntidad() { return entidad; }
    public Class<? extends ProductoDTO> getDto() { return dto; }

    public static Optional<CategoriaProducto> desdeSlug(String slug) {
        return Arrays.stream(values()).filter(c -> c.slug.equals(slug)).findFirst();
    }

    public static CategoriaProducto de(Producto producto) {
        return Arrays.stream(values())
                .filter(c -> c.entidad.isInstance(producto))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Producto sin categoría: " + producto.getClass()));
    }
}
