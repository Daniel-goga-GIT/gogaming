package org.iesalixar.daw2.alejandromontero.ticket_logger_api.mappers;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.AlfombrillaDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Alfombrilla;
import org.springframework.stereotype.Component;

@Component
public class AlfombrillaMapper {

    public AlfombrillaDTO toDTO(Alfombrilla alfombrilla) {
        if (alfombrilla == null) return null;

        AlfombrillaDTO dto = new AlfombrillaDTO();
        dto.setId(alfombrilla.getId());
        dto.setNombre(alfombrilla.getNombre());
        dto.setMarca(alfombrilla.getMarca());
        dto.setFechaPublicacion(alfombrilla.getFechaPublicacion());
        dto.setPrecio(alfombrilla.getPrecio());
        dto.setStock(alfombrilla.getStock());
        dto.setTipoProducto(alfombrilla.getTipoProducto());
        dto.setImage(alfombrilla.getImage());
        dto.setDimensiones(alfombrilla.getDimensiones());
        dto.setGrosor(alfombrilla.getGrosor());
        dto.setSuperficie(alfombrilla.getSuperficie());
        return dto;
    }

    public Alfombrilla toEntity(AlfombrillaDTO dto) {
        if (dto == null) return null;

        Alfombrilla alfombrilla = new Alfombrilla();
        alfombrilla.setId(dto.getId());
        alfombrilla.setNombre(dto.getNombre());
        alfombrilla.setMarca(dto.getMarca());
        alfombrilla.setFechaPublicacion(dto.getFechaPublicacion());
        alfombrilla.setPrecio(dto.getPrecio());
        alfombrilla.setStock(dto.getStock());
        alfombrilla.setTipoProducto(dto.getTipoProducto());
        alfombrilla.setImage(dto.getImage());
        alfombrilla.setDimensiones(dto.getDimensiones());
        alfombrilla.setGrosor(dto.getGrosor());
        alfombrilla.setSuperficie(dto.getSuperficie());
        return alfombrilla;
    }
}
