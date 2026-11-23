package org.iesalixar.daw2.alejandromontero.ticket_logger_api.services;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.TecladoDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Teclado;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.mappers.TecladoMapper;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories.TecladoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TecladoService {

    @Autowired
    private TecladoRepository tecladoRepository;

    @Autowired
    private TecladoMapper tecladoMapper;

    public Page<TecladoDTO> getAllTeclados(String nombre, Pageable pageable) {
        String normalizedNombre = (nombre == null) ? null : nombre.trim();
        return tecladoRepository.findTeclados(normalizedNombre, pageable)
                .map(tecladoMapper::toDTO);
    }

    public List<TecladoDTO> getHomeTeclado() {
        return tecladoRepository.getHomeProductos();
    }

    public TecladoDTO getTecladoById(Long id) {
        return tecladoRepository.findTecladoDTOById(id).orElse(null);
    }

    public TecladoDTO createTeclado(TecladoDTO tecladoDTO) {
        validateTeclado(tecladoDTO);
        Teclado teclado = tecladoMapper.toEntity(tecladoDTO);
        teclado.setId(null);
        Teclado savedTeclado = tecladoRepository.save(teclado);
        return tecladoMapper.toDTO(savedTeclado);
    }

    public TecladoDTO updateTeclado(Long id, TecladoDTO tecladoDTO) {
        validateTeclado(tecladoDTO);
        Optional<Teclado> existingTeclado = tecladoRepository.findById(id);
        if (existingTeclado.isPresent()) {
            Teclado teclado = existingTeclado.get();
            teclado.setNombre(tecladoDTO.getNombre());
            teclado.setMarca(tecladoDTO.getMarca());
            teclado.setFechaPublicacion(tecladoDTO.getFechaPublicacion());
            teclado.setPrecio(tecladoDTO.getPrecio());
            teclado.setStock(tecladoDTO.getStock());
            teclado.setTipoProducto(tecladoDTO.getTipoProducto());
            teclado.setImage(tecladoDTO.getImage());
            teclado.setTipoSwitch(tecladoDTO.getTipoSwitch());
            teclado.setLayout(tecladoDTO.getLayout());
            teclado.setConectividad(tecladoDTO.getConectividad());
            teclado.setRetroiluminacion(tecladoDTO.getRetroiluminacion());
            Teclado updatedTeclado = tecladoRepository.save(teclado);
            return tecladoMapper.toDTO(updatedTeclado);
        }
        return null;
    }

    private void validateTeclado(TecladoDTO teclado) {
        if (teclado.getNombre() == null || teclado.getNombre().isEmpty()) throw new IllegalArgumentException("Nombre es obligatorio");
        if (teclado.getMarca() == null || teclado.getMarca().isEmpty()) throw new IllegalArgumentException("Marca es obligatorio");
        if (teclado.getPrecio() == null) throw new IllegalArgumentException("Precio es obligatorio");
        if (teclado.getStock() == null) throw new IllegalArgumentException("Stock es obligatorio");
        if (teclado.getImage() == null || teclado.getImage().isEmpty()) throw new IllegalArgumentException("Imagen es obligatoria");
        if (teclado.getTipoSwitch() == null || teclado.getTipoSwitch().isEmpty()) throw new IllegalArgumentException("Tipo de switch es obligatorio");
        if (teclado.getLayout() == null || teclado.getLayout().isEmpty()) throw new IllegalArgumentException("Layout es obligatorio");
        if (teclado.getConectividad() == null || teclado.getConectividad().isEmpty()) throw new IllegalArgumentException("Conectividad es obligatoria");
    }

    public void deleteTeclado(Long id) {
        tecladoRepository.deleteById(id);
    }
}
