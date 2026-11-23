package org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.AuricularesDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Auriculares;
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
public interface AuricularesRepository extends JpaRepository<Auriculares, Long> {

    @Query("SELECT a FROM Auriculares a " +
            "WHERE (:nombre IS NULL OR :nombre = '' OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) OR LOWER(a.marca) LIKE LOWER(CONCAT('%', :nombre, '%')))")
    Page<Auriculares> findAuriculares(@Param("nombre") String nombre, Pageable pageable);

    @Query(value = "SELECT new org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.AuricularesDTO(" +
            "a.id, a.nombre, a.marca, a.fechaPublicacion, a.precio, a.stock, a.tipoProducto, a.image, " +
            "a.tipo, a.driver, a.conectividad, a.impedancia) " +
            "FROM Auriculares a",
            countQuery = "SELECT count(a) FROM Auriculares a")
    Page<AuricularesDTO> findAllAuricularesDTOs(Pageable pageable);

    @Query("SELECT new org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.AuricularesDTO(" +
            "a.id, a.nombre, a.marca, a.fechaPublicacion, a.precio, a.stock, a.tipoProducto, a.image, " +
            "a.tipo, a.driver, a.conectividad, a.impedancia) " +
            "FROM Auriculares a WHERE a.id = :id")
    Optional<AuricularesDTO> findAuricularesDTOById(@Param("id") Long id);

    default List<AuricularesDTO> getHomeProductos() {
        return findAllAuricularesDTOs(PageRequest.of(0, 5)).getContent();
    }
}
