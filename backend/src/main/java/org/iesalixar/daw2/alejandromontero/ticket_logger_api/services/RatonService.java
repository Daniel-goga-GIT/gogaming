package org.iesalixar.daw2.alejandromontero.ticket_logger_api.services;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.RatonDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Raton;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.mappers.RatonMapper;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories.RatonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RatonService {

    @Autowired
    private RatonRepository ratonRepository;

    @Autowired
    private RatonMapper ratonMapper;

    public Page<RatonDTO> getAllRatones(String nombre, Pageable pageable) {
        String normalizedNombre = (nombre == null) ? null : nombre.trim();
        return ratonRepository.findRatones(normalizedNombre, pageable)
                .map(ratonMapper::toDTO);
    }

    public List<RatonDTO> getHomeRaton() {
        return ratonRepository.getHomeProductos();
    }

    //Lógica para poder ver productos por id
    public RatonDTO getRatonById(Long id) {
        return ratonRepository.findRatonDTOById(id).orElse(null);
    }

    public RatonDTO createRaton(RatonDTO ratonDTO) {
        validateRaton(ratonDTO);
        Raton raton = ratonMapper.toEntity(ratonDTO);
        raton.setId(null); // Asegurar que sea nulo para creación auto-incremental
        Raton savedRaton = ratonRepository.save(raton);
        return ratonMapper.toDTO(savedRaton);
    }

    public RatonDTO updateRaton(Long id, RatonDTO ratonDTO) {
        validateRaton(ratonDTO);
        Optional<Raton> existingRaton = ratonRepository.findById(id);
        if (existingRaton.isPresent()) {
            Raton raton = existingRaton.get();
            raton.setNombre(ratonDTO.getNombre());
            raton.setMarca(ratonDTO.getMarca());
            raton.setFechaPublicacion(ratonDTO.getFechaPublicacion());
            raton.setPrecio(ratonDTO.getPrecio());
            raton.setStock(ratonDTO.getStock());
            raton.setTipoProducto(ratonDTO.getTipoProducto());
            raton.setImage(ratonDTO.getImage());
            raton.setTipoAgarre(ratonDTO.getTipoAgarre());
            raton.setMedidas(ratonDTO.getMedidas());
            raton.setSensor(ratonDTO.getSensor());
            raton.setPeso(ratonDTO.getPeso());
            raton.setSwitches(ratonDTO.getSwitches());
            Raton updatedRaton = ratonRepository.save(raton);
            return ratonMapper.toDTO(updatedRaton);
        }
        return null;
    }

    private void validateRaton(RatonDTO raton) {
        if (raton.getNombre() == null || raton.getNombre().isEmpty()) throw new IllegalArgumentException("Nombre es obligatorio");
        if (raton.getMarca() == null || raton.getMarca().isEmpty()) throw new IllegalArgumentException("Marca es obligatorio");
        if (raton.getPrecio() == null) throw new IllegalArgumentException("Precio es obligatorio");
        if (raton.getStock() == null) throw new IllegalArgumentException("Stock es obligatorio");
        if (raton.getImage() == null || raton.getImage().isEmpty()) throw new IllegalArgumentException("Imagen es obligatoria");
        if (raton.getTipoAgarre() == null) throw new IllegalArgumentException("Tipo de agarre es obligatorio");
        if (raton.getSensor() == null || raton.getSensor().isEmpty()) throw new IllegalArgumentException("Sensor es obligatorio");
        if (raton.getPeso() == null) throw new IllegalArgumentException("Peso es obligatorio");
        if (raton.getSwitches() == null || raton.getSwitches().isEmpty()) throw new IllegalArgumentException("Switches son obligatorios");
        if (raton.getMedidas() == null || raton.getMedidas().isEmpty()) throw new IllegalArgumentException("Medidas son obligatorias");
    }

    public void deleteRaton(Long id) {
        ratonRepository.deleteById(id);
    }
}
