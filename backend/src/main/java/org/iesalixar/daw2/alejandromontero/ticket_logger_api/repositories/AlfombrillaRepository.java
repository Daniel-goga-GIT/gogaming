package org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.AlfombrillaDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Alfombrilla;
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
public interface AlfombrillaRepository extends JpaRepository<Alfombrilla, Long> {

    @Query("SELECT a FROM Alfombrilla a " +
            "WHERE (:nombre IS NULL OR :nombre = '' OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) OR LOWER(a.marca) LIKE LOWER(CONCAT('%', :nombre, '%')))")
    Page<Alfombrilla> findAlfombrillas(@Param("nombre") String nombre, Pageable pageable);

    @Query(value = "SELECT new org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.AlfombrillaDTO(" +
            "a.id, a.nombre, a.marca, a.fechaPublicacion, a.precio, a.stock, a.tipoProducto, a.image, " +
            "a.dimensiones, a.grosor, a.superficie) " +
            "FROM Alfombrilla a",
            countQuery = "SELECT count(a) FROM Alfombrilla a")
    Page<AlfombrillaDTO> findAllAlfombrillaDTOs(Pageable pageable);

    @Query("SELECT new org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.AlfombrillaDTO(" +
            "a.id, a.nombre, a.marca, a.fechaPublicacion, a.precio, a.stock, a.tipoProducto, a.image, " +
            "a.dimensiones, a.grosor, a.superficie) " +
            "FROM Alfombrilla a WHERE a.id = :id")
    Optional<AlfombrillaDTO> findAlfombrillaDTOById(@Param("id") Long id);

    default List<AlfombrillaDTO> getHomeProductos() {
        return findAllAlfombrillaDTOs(PageRequest.of(0, 5)).getContent();
    }
}
