package org.iesalixar.daw2.alejandromontero.ticket_logger_api.mappers;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.AuricularesDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Auriculares;
import org.springframework.stereotype.Component;

@Component
public class AuricularesMapper {

    public AuricularesDTO toDTO(Auriculares auriculares) {
        if (auriculares == null) return null;

        AuricularesDTO dto = new AuricularesDTO();
        dto.setId(auriculares.getId());
        dto.setNombre(auriculares.getNombre());
        dto.setMarca(auriculares.getMarca());
        dto.setFechaPublicacion(auriculares.getFechaPublicacion());
        dto.setPrecio(auriculares.getPrecio());
        dto.setStock(auriculares.getStock());
        dto.setTipoProducto(auriculares.getTipoProducto());
        dto.setImage(auriculares.getImage());
        dto.setTipo(auriculares.getTipo());
        dto.setDriver(auriculares.getDriver());
        dto.setConectividad(auriculares.getConectividad());
        dto.setImpedancia(auriculares.getImpedancia());
        return dto;
    }

    public Auriculares toEntity(AuricularesDTO dto) {
        if (dto == null) return null;

        Auriculares auriculares = new Auriculares();
        auriculares.setId(dto.getId());
        auriculares.setNombre(dto.getNombre());
        auriculares.setMarca(dto.getMarca());
        auriculares.setFechaPublicacion(dto.getFechaPublicacion());
        auriculares.setPrecio(dto.getPrecio());
        auriculares.setStock(dto.getStock());
        auriculares.setTipoProducto(dto.getTipoProducto());
        auriculares.setImage(dto.getImage());
        auriculares.setTipo(dto.getTipo());
        auriculares.setDriver(dto.getDriver());
        auriculares.setConectividad(dto.getConectividad());
        auriculares.setImpedancia(dto.getImpedancia());
        return auriculares;
    }
}
