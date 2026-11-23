package org.iesalixar.daw2.alejandromontero.ticket_logger_api.mappers;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.MangaDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Manga;
import org.springframework.stereotype.Component;

@Component
public class MangaMapper {

    public MangaDTO toDTO(Manga manga) {
        if (manga == null) return null;

        MangaDTO dto = new MangaDTO();
        dto.setId(manga.getId());
        dto.setNombre(manga.getNombre());
        dto.setMarca(manga.getMarca());
        dto.setFechaPublicacion(manga.getFechaPublicacion());
        dto.setPrecio(manga.getPrecio());
        dto.setStock(manga.getStock());
        dto.setTipoProducto(manga.getTipoProducto());
        dto.setImage(manga.getImage());
        dto.setTalla(manga.getTalla());
        dto.setMaterial(manga.getMaterial());
        dto.setLargo(manga.getLargo());
        dto.setColor(manga.getColor());
        return dto;
    }

    public Manga toEntity(MangaDTO dto) {
        if (dto == null) return null;

        Manga manga = new Manga();
        manga.setId(dto.getId());
        manga.setNombre(dto.getNombre());
        manga.setMarca(dto.getMarca());
        manga.setFechaPublicacion(dto.getFechaPublicacion());
        manga.setPrecio(dto.getPrecio());
        manga.setStock(dto.getStock());
        manga.setTipoProducto(dto.getTipoProducto());
        manga.setImage(dto.getImage());
        manga.setTalla(dto.getTalla());
        manga.setMaterial(dto.getMaterial());
        manga.setLargo(dto.getLargo());
        manga.setColor(dto.getColor());
        return manga;
    }
}
