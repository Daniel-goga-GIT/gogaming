package org.iesalixar.daw2.alejandromontero.ticket_logger_api.services;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.MangaDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Manga;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.mappers.MangaMapper;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories.MangaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MangaService {

    @Autowired
    private MangaRepository mangaRepository;

    @Autowired
    private MangaMapper mangaMapper;

    public Page<MangaDTO> getAllMangas(String nombre, Pageable pageable) {
        String normalizedNombre = (nombre == null) ? null : nombre.trim();
        return mangaRepository.findMangas(normalizedNombre, pageable)
                .map(mangaMapper::toDTO);
    }

    public List<MangaDTO> getHomeManga() {
        return mangaRepository.getHomeProductos();
    }

    public MangaDTO getMangaById(Long id) {
        return mangaRepository.findMangaDTOById(id).orElse(null);
    }

    public MangaDTO createManga(MangaDTO mangaDTO) {
        validateManga(mangaDTO);
        Manga manga = mangaMapper.toEntity(mangaDTO);
        manga.setId(null);
        Manga savedManga = mangaRepository.save(manga);
        return mangaMapper.toDTO(savedManga);
    }

    public MangaDTO updateManga(Long id, MangaDTO mangaDTO) {
        validateManga(mangaDTO);
        Optional<Manga> existingManga = mangaRepository.findById(id);
        if (existingManga.isPresent()) {
            Manga manga = existingManga.get();
            manga.setNombre(mangaDTO.getNombre());
            manga.setMarca(mangaDTO.getMarca());
            manga.setFechaPublicacion(mangaDTO.getFechaPublicacion());
            manga.setPrecio(mangaDTO.getPrecio());
            manga.setStock(mangaDTO.getStock());
            manga.setTipoProducto(mangaDTO.getTipoProducto());
            manga.setImage(mangaDTO.getImage());
            manga.setTalla(mangaDTO.getTalla());
            manga.setMaterial(mangaDTO.getMaterial());
            manga.setLargo(mangaDTO.getLargo());
            manga.setColor(mangaDTO.getColor());
            Manga updatedManga = mangaRepository.save(manga);
            return mangaMapper.toDTO(updatedManga);
        }
        return null;
    }

    private void validateManga(MangaDTO manga) {
        if (manga.getNombre() == null || manga.getNombre().isEmpty()) throw new IllegalArgumentException("Nombre es obligatorio");
        if (manga.getMarca() == null || manga.getMarca().isEmpty()) throw new IllegalArgumentException("Marca es obligatorio");
        if (manga.getPrecio() == null) throw new IllegalArgumentException("Precio es obligatorio");
        if (manga.getStock() == null) throw new IllegalArgumentException("Stock es obligatorio");
        if (manga.getImage() == null || manga.getImage().isEmpty()) throw new IllegalArgumentException("Imagen es obligatoria");
        if (manga.getTalla() == null || manga.getTalla().isEmpty()) throw new IllegalArgumentException("Talla es obligatoria");
        if (manga.getMaterial() == null || manga.getMaterial().isEmpty()) throw new IllegalArgumentException("Material es obligatorio");
        if (manga.getColor() == null || manga.getColor().isEmpty()) throw new IllegalArgumentException("Color es obligatorio");
    }

    public void deleteManga(Long id) {
        mangaRepository.deleteById(id);
    }
}
