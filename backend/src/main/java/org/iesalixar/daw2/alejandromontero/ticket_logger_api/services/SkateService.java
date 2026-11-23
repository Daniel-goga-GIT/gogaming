package org.iesalixar.daw2.alejandromontero.ticket_logger_api.services;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.SkateDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Skate;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.mappers.SkateMapper;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories.SkateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SkateService {

    @Autowired
    private SkateRepository skateRepository;

    @Autowired
    private SkateMapper skateMapper;

    public Page<SkateDTO> getAllSkates(String nombre, Pageable pageable) {
        String normalizedNombre = (nombre == null) ? null : nombre.trim();
        return skateRepository.findSkates(normalizedNombre, pageable)
                .map(skateMapper::toDTO);
    }

    public List<SkateDTO> getHomeSkate() {
        return skateRepository.getHomeProductos();
    }

    public SkateDTO getSkateById(Long id) {
        return skateRepository.findSkateDTOById(id).orElse(null);
    }

    public SkateDTO createSkate(SkateDTO skateDTO) {
        validateSkate(skateDTO);
        Skate skate = skateMapper.toEntity(skateDTO);
        skate.setId(null);
        Skate savedSkate = skateRepository.save(skate);
        return skateMapper.toDTO(savedSkate);
    }

    public SkateDTO updateSkate(Long id, SkateDTO skateDTO) {
        validateSkate(skateDTO);
        Optional<Skate> existingSkate = skateRepository.findById(id);
        if (existingSkate.isPresent()) {
            Skate skate = existingSkate.get();
            skate.setNombre(skateDTO.getNombre());
            skate.setMarca(skateDTO.getMarca());
            skate.setFechaPublicacion(skateDTO.getFechaPublicacion());
            skate.setPrecio(skateDTO.getPrecio());
            skate.setStock(skateDTO.getStock());
            skate.setTipoProducto(skateDTO.getTipoProducto());
            skate.setImage(skateDTO.getImage());
            skate.setMaterial(skateDTO.getMaterial());
            skate.setGrosor(skateDTO.getGrosor());
            skate.setCompatibilidad(skateDTO.getCompatibilidad());
            Skate updatedSkate = skateRepository.save(skate);
            return skateMapper.toDTO(updatedSkate);
        }
        return null;
    }

    private void validateSkate(SkateDTO skate) {
        if (skate.getNombre() == null || skate.getNombre().isEmpty()) throw new IllegalArgumentException("Nombre es obligatorio");
        if (skate.getMarca() == null || skate.getMarca().isEmpty()) throw new IllegalArgumentException("Marca es obligatorio");
        if (skate.getPrecio() == null) throw new IllegalArgumentException("Precio es obligatorio");
        if (skate.getStock() == null) throw new IllegalArgumentException("Stock es obligatorio");
        if (skate.getImage() == null || skate.getImage().isEmpty()) throw new IllegalArgumentException("Imagen es obligatoria");
        if (skate.getMaterial() == null || skate.getMaterial().isEmpty()) throw new IllegalArgumentException("Material es obligatorio");
        if (skate.getCompatibilidad() == null || skate.getCompatibilidad().isEmpty()) throw new IllegalArgumentException("Compatibilidad es obligatoria");
    }

    public void deleteSkate(Long id) {
        skateRepository.deleteById(id);
    }
}
