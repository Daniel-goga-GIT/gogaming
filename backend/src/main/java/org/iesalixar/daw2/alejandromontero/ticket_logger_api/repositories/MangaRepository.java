package org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.MangaDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Manga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MangaRepository extends JpaRepository<Manga, Long> {

    @Query("SELECT m FROM Manga m " +
            "WHERE (:nombre IS NULL OR :nombre = '' OR LOWER(m.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) OR LOWER(m.marca) LIKE LOWER(CONCAT('%', :nombre, '%')))")
    Page<Manga> findMangas(@Param("nombre") String nombre, Pageable pageable);

    @Query(value = "SELECT new org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.MangaDTO(" +
            "m.id, m.nombre, m.marca, m.fechaPublicacion, m.precio, m.stock, m.tipoProducto, m.image, " +
            "m.talla, m.material, m.largo, m.color) " +
            "FROM Manga m",
            countQuery = "SELECT count(m) FROM Manga m")
    Page<MangaDTO> findAllMangaDTOs(Pageable pageable);

    @Query("SELECT new org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.MangaDTO(" +
            "m.id, m.nombre, m.marca, m.fechaPublicacion, m.precio, m.stock, m.tipoProducto, m.image, " +
            "m.talla, m.material, m.largo, m.color) " +
            "FROM Manga m WHERE m.id = :id")
    Optional<MangaDTO> findMangaDTOById(@Param("id") Long id);

    default List<MangaDTO> getHomeProductos() {
        return findAllMangaDTOs(PageRequest.of(0, 5)).getContent();
    }
}
