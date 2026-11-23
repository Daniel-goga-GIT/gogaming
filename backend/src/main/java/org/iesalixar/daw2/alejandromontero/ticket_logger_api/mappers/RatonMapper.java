package org.iesalixar.daw2.alejandromontero.ticket_logger_api.mappers;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.RatonDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Raton;
import org.springframework.stereotype.Component;

@Component
public class RatonMapper {

    public RatonDTO toDTO(Raton raton) {
        if (raton == null) {
            return null;
        }

        RatonDTO dto = new RatonDTO();
        dto.setId(raton.getId());
        dto.setNombre(raton.getNombre());
        dto.setMarca(raton.getMarca());
        dto.setFechaPublicacion(raton.getFechaPublicacion());
        dto.setPrecio(raton.getPrecio());
        dto.setStock(raton.getStock());
        dto.setTipoProducto(raton.getTipoProducto());
        dto.setImage(raton.getImage());
        
        dto.setTipoAgarre(raton.getTipoAgarre());
        dto.setMedidas(raton.getMedidas());
        dto.setSensor(raton.getSensor());
        dto.setPeso(raton.getPeso());
        dto.setSwitches(raton.getSwitches());

        return dto;
    }

    public Raton toEntity(RatonDTO dto) {
        if (dto == null) {
            return null;
        }

        Raton raton = new Raton();
        raton.setId(dto.getId());
        raton.setNombre(dto.getNombre());
        raton.setMarca(dto.getMarca());
        raton.setFechaPublicacion(dto.getFechaPublicacion());
        raton.setPrecio(dto.getPrecio());
        raton.setStock(dto.getStock());
        raton.setTipoProducto(dto.getTipoProducto());
        raton.setImage(dto.getImage());
        
        raton.setTipoAgarre(dto.getTipoAgarre());
        raton.setMedidas(dto.getMedidas());
        raton.setSensor(dto.getSensor());
        raton.setPeso(dto.getPeso());
        raton.setSwitches(dto.getSwitches());

        return raton;
    }
}
