package org.iesalixar.daw2.alejandromontero.ticket_logger_api.mappers;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.SkateDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Skate;
import org.springframework.stereotype.Component;

@Component
public class SkateMapper {

    public SkateDTO toDTO(Skate skate) {
        if (skate == null) return null;

        SkateDTO dto = new SkateDTO();
        dto.setId(skate.getId());
        dto.setNombre(skate.getNombre());
        dto.setMarca(skate.getMarca());
        dto.setFechaPublicacion(skate.getFechaPublicacion());
        dto.setPrecio(skate.getPrecio());
        dto.setStock(skate.getStock());
        dto.setTipoProducto(skate.getTipoProducto());
        dto.setImage(skate.getImage());
        dto.setMaterial(skate.getMaterial());
        dto.setGrosor(skate.getGrosor());
        dto.setCompatibilidad(skate.getCompatibilidad());
        return dto;
    }

    public Skate toEntity(SkateDTO dto) {
        if (dto == null) return null;

        Skate skate = new Skate();
        skate.setId(dto.getId());
        skate.setNombre(dto.getNombre());
        skate.setMarca(dto.getMarca());
        skate.setFechaPublicacion(dto.getFechaPublicacion());
        skate.setPrecio(dto.getPrecio());
        skate.setStock(dto.getStock());
        skate.setTipoProducto(dto.getTipoProducto());
        skate.setImage(dto.getImage());
        skate.setMaterial(dto.getMaterial());
        skate.setGrosor(dto.getGrosor());
        skate.setCompatibilidad(dto.getCompatibilidad());
        return skate;
    }
}
