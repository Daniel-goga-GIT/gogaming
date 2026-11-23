package org.iesalixar.daw2.alejandromontero.ticket_logger_api.mappers;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.TecladoDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Teclado;
import org.springframework.stereotype.Component;

@Component
public class TecladoMapper {

    public TecladoDTO toDTO(Teclado teclado) {
        if (teclado == null) return null;

        TecladoDTO dto = new TecladoDTO();
        dto.setId(teclado.getId());
        dto.setNombre(teclado.getNombre());
        dto.setMarca(teclado.getMarca());
        dto.setFechaPublicacion(teclado.getFechaPublicacion());
        dto.setPrecio(teclado.getPrecio());
        dto.setStock(teclado.getStock());
        dto.setTipoProducto(teclado.getTipoProducto());
        dto.setImage(teclado.getImage());
        dto.setTipoSwitch(teclado.getTipoSwitch());
        dto.setLayout(teclado.getLayout());
        dto.setConectividad(teclado.getConectividad());
        dto.setRetroiluminacion(teclado.getRetroiluminacion());
        return dto;
    }

    public Teclado toEntity(TecladoDTO dto) {
        if (dto == null) return null;

        Teclado teclado = new Teclado();
        teclado.setId(dto.getId());
        teclado.setNombre(dto.getNombre());
        teclado.setMarca(dto.getMarca());
        teclado.setFechaPublicacion(dto.getFechaPublicacion());
        teclado.setPrecio(dto.getPrecio());
        teclado.setStock(dto.getStock());
        teclado.setTipoProducto(dto.getTipoProducto());
        teclado.setImage(dto.getImage());
        teclado.setTipoSwitch(dto.getTipoSwitch());
        teclado.setLayout(dto.getLayout());
        teclado.setConectividad(dto.getConectividad());
        teclado.setRetroiluminacion(dto.getRetroiluminacion());
        return teclado;
    }
}
