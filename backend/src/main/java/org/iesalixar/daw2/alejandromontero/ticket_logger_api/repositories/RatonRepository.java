package org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.RatonDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Raton;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatonRepository extends JpaRepository<Raton, Long> {

    @Query("SELECT r FROM Raton r " +
            "WHERE (:nombre IS NULL OR :nombre = '' OR LOWER(r.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) OR LOWER(r.marca) LIKE LOWER(CONCAT('%', :nombre, '%')))")
    Page<Raton> findRatones(@Param("nombre") String nombre, Pageable pageable);

    @Query(value = "SELECT new org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.RatonDTO(" +
            "r.id, r.nombre, r.marca, r.fechaPublicacion, r.precio, r.stock, r.tipoProducto, r.image, " +
            "r.tipoAgarre, r.medidas, r.sensor, r.peso, r.switches) " +
            "FROM Raton r",
            countQuery = "SELECT count(r) FROM Raton r")
    Page<RatonDTO> findAllRatonDTOs(Pageable pageable);

    @Query("SELECT new org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.RatonDTO(" +
            "r.id, r.nombre, r.marca, r.fechaPublicacion, r.precio, r.stock, r.tipoProducto, r.image, " +
            "r.tipoAgarre, r.medidas, r.sensor, r.peso, r.switches) " +
            "FROM Raton r WHERE r.id = :id")
    java.util.Optional<RatonDTO> findRatonDTOById(@org.springframework.data.repository.query.Param("id") Long id);

    default List<RatonDTO> getHomeProductos() {
        return findAllRatonDTOs(PageRequest.of(0, 5)).getContent();
    }
}
