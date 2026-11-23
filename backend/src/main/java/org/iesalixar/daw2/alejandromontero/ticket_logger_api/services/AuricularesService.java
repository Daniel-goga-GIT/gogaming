package org.iesalixar.daw2.alejandromontero.ticket_logger_api.services;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.AuricularesDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Auriculares;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.mappers.AuricularesMapper;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories.AuricularesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuricularesService {

    @Autowired
    private AuricularesRepository auricularesRepository;

    @Autowired
    private AuricularesMapper auricularesMapper;

    public Page<AuricularesDTO> getAllAuriculares(String nombre, Pageable pageable) {
        String normalizedNombre = (nombre == null) ? null : nombre.trim();
        return auricularesRepository.findAuriculares(normalizedNombre, pageable)
                .map(auricularesMapper::toDTO);
    }

    public List<AuricularesDTO> getHomeAuriculares() {
        return auricularesRepository.getHomeProductos();
    }

    public AuricularesDTO getAuricularesById(Long id) {
        return auricularesRepository.findAuricularesDTOById(id).orElse(null);
    }

    public AuricularesDTO createAuriculares(AuricularesDTO auricularesDTO) {
        validateAuriculares(auricularesDTO);
        Auriculares auriculares = auricularesMapper.toEntity(auricularesDTO);
        auriculares.setId(null);
        Auriculares savedAuriculares = auricularesRepository.save(auriculares);
        return auricularesMapper.toDTO(savedAuriculares);
    }

    public AuricularesDTO updateAuriculares(Long id, AuricularesDTO auricularesDTO) {
        validateAuriculares(auricularesDTO);
        Optional<Auriculares> existingAuriculares = auricularesRepository.findById(id);
        if (existingAuriculares.isPresent()) {
            Auriculares auriculares = existingAuriculares.get();
            auriculares.setNombre(auricularesDTO.getNombre());
            auriculares.setMarca(auricularesDTO.getMarca());
            auriculares.setFechaPublicacion(auricularesDTO.getFechaPublicacion());
            auriculares.setPrecio(auricularesDTO.getPrecio());
            auriculares.setStock(auricularesDTO.getStock());
            auriculares.setTipoProducto(auricularesDTO.getTipoProducto());
            auriculares.setImage(auricularesDTO.getImage());
            auriculares.setTipo(auricularesDTO.getTipo());
            auriculares.setDriver(auricularesDTO.getDriver());
            auriculares.setConectividad(auricularesDTO.getConectividad());
            auriculares.setImpedancia(auricularesDTO.getImpedancia());
            Auriculares updatedAuriculares = auricularesRepository.save(auriculares);
            return auricularesMapper.toDTO(updatedAuriculares);
        }
        return null;
    }

    private void validateAuriculares(AuricularesDTO auriculares) {
        if (auriculares.getNombre() == null || auriculares.getNombre().isEmpty()) throw new IllegalArgumentException("Nombre es obligatorio");
        if (auriculares.getMarca() == null || auriculares.getMarca().isEmpty()) throw new IllegalArgumentException("Marca es obligatorio");
        if (auriculares.getPrecio() == null) throw new IllegalArgumentException("Precio es obligatorio");
        if (auriculares.getStock() == null) throw new IllegalArgumentException("Stock es obligatorio");
        if (auriculares.getImage() == null || auriculares.getImage().isEmpty()) throw new IllegalArgumentException("Imagen es obligatoria");
        if (auriculares.getTipo() == null || auriculares.getTipo().isEmpty()) throw new IllegalArgumentException("Tipo es obligatorio");
        if (auriculares.getDriver() == null || auriculares.getDriver().isEmpty()) throw new IllegalArgumentException("Driver es obligatorio");
        if (auriculares.getConectividad() == null || auriculares.getConectividad().isEmpty()) throw new IllegalArgumentException("Conectividad es obligatoria");
    }

    public void deleteAuriculares(Long id) {
        auricularesRepository.deleteById(id);
    }
}
