package org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.SkateDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Skate;
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
public interface SkateRepository extends JpaRepository<Skate, Long> {

    @Query("SELECT s FROM Skate s " +
            "WHERE (:nombre IS NULL OR :nombre = '' OR LOWER(s.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) OR LOWER(s.marca) LIKE LOWER(CONCAT('%', :nombre, '%')))")
    Page<Skate> findSkates(@Param("nombre") String nombre, Pageable pageable);

    @Query(value = "SELECT new org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.SkateDTO(" +
            "s.id, s.nombre, s.marca, s.fechaPublicacion, s.precio, s.stock, s.tipoProducto, s.image, " +
            "s.material, s.grosor, s.compatibilidad) " +
            "FROM Skate s",
            countQuery = "SELECT count(s) FROM Skate s")
    Page<SkateDTO> findAllSkateDTOs(Pageable pageable);

    @Query("SELECT new org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.SkateDTO(" +
            "s.id, s.nombre, s.marca, s.fechaPublicacion, s.precio, s.stock, s.tipoProducto, s.image, " +
            "s.material, s.grosor, s.compatibilidad) " +
            "FROM Skate s WHERE s.id = :id")
    Optional<SkateDTO> findSkateDTOById(@Param("id") Long id);

    default List<SkateDTO> getHomeProductos() {
        return findAllSkateDTOs(PageRequest.of(0, 5)).getContent();
    }
}
