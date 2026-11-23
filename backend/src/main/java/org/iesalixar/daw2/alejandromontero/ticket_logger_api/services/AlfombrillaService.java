package org.iesalixar.daw2.alejandromontero.ticket_logger_api.services;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.AlfombrillaDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Alfombrilla;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.mappers.AlfombrillaMapper;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories.AlfombrillaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlfombrillaService {

    @Autowired
    private AlfombrillaRepository alfombrillaRepository;

    @Autowired
    private AlfombrillaMapper alfombrillaMapper;

    public Page<AlfombrillaDTO> getAllAlfombrillas(String nombre, Pageable pageable) {
        String normalizedNombre = (nombre == null) ? null : nombre.trim();
        return alfombrillaRepository.findAlfombrillas(normalizedNombre, pageable)
                .map(alfombrillaMapper::toDTO);
    }

    public List<AlfombrillaDTO> getHomeAlfombrilla() {
        return alfombrillaRepository.getHomeProductos();
    }

    public AlfombrillaDTO getAlfombrillaById(Long id) {
        return alfombrillaRepository.findAlfombrillaDTOById(id).orElse(null);
    }

    public AlfombrillaDTO createAlfombrilla(AlfombrillaDTO alfombrillaDTO) {
        validateAlfombrilla(alfombrillaDTO);
        Alfombrilla alfombrilla = alfombrillaMapper.toEntity(alfombrillaDTO);
        alfombrilla.setId(null);
        Alfombrilla savedAlfombrilla = alfombrillaRepository.save(alfombrilla);
        return alfombrillaMapper.toDTO(savedAlfombrilla);
    }

    public AlfombrillaDTO updateAlfombrilla(Long id, AlfombrillaDTO alfombrillaDTO) {
        validateAlfombrilla(alfombrillaDTO);
        Optional<Alfombrilla> existingAlfombrilla = alfombrillaRepository.findById(id);
        if (existingAlfombrilla.isPresent()) {
            Alfombrilla alfombrilla = existingAlfombrilla.get();
            alfombrilla.setNombre(alfombrillaDTO.getNombre());
            alfombrilla.setMarca(alfombrillaDTO.getMarca());
            alfombrilla.setFechaPublicacion(alfombrillaDTO.getFechaPublicacion());
            alfombrilla.setPrecio(alfombrillaDTO.getPrecio());
            alfombrilla.setStock(alfombrillaDTO.getStock());
            alfombrilla.setTipoProducto(alfombrillaDTO.getTipoProducto());
            alfombrilla.setImage(alfombrillaDTO.getImage());
            alfombrilla.setDimensiones(alfombrillaDTO.getDimensiones());
            alfombrilla.setGrosor(alfombrillaDTO.getGrosor());
            alfombrilla.setSuperficie(alfombrillaDTO.getSuperficie());
            Alfombrilla updatedAlfombrilla = alfombrillaRepository.save(alfombrilla);
            return alfombrillaMapper.toDTO(updatedAlfombrilla);
        }
        return null;
    }

    private void validateAlfombrilla(AlfombrillaDTO alfombrilla) {
        if (alfombrilla.getNombre() == null || alfombrilla.getNombre().isEmpty()) throw new IllegalArgumentException("Nombre es obligatorio");
        if (alfombrilla.getMarca() == null || alfombrilla.getMarca().isEmpty()) throw new IllegalArgumentException("Marca es obligatorio");
        if (alfombrilla.getPrecio() == null) throw new IllegalArgumentException("Precio es obligatorio");
        if (alfombrilla.getStock() == null) throw new IllegalArgumentException("Stock es obligatorio");
        if (alfombrilla.getImage() == null || alfombrilla.getImage().isEmpty()) throw new IllegalArgumentException("Imagen es obligatoria");
        if (alfombrilla.getDimensiones() == null || alfombrilla.getDimensiones().isEmpty()) throw new IllegalArgumentException("Dimensiones son obligatorias");
        if (alfombrilla.getSuperficie() == null || alfombrilla.getSuperficie().isEmpty()) throw new IllegalArgumentException("Superficie es obligatoria");
    }

    public void deleteAlfombrilla(Long id) {
        alfombrillaRepository.deleteById(id);
    }
}
