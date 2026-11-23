package org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.TecladoDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Teclado;
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
public interface TecladoRepository extends JpaRepository<Teclado, Long> {

    @Query("SELECT t FROM Teclado t " +
            "WHERE (:nombre IS NULL OR :nombre = '' OR LOWER(t.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) OR LOWER(t.marca) LIKE LOWER(CONCAT('%', :nombre, '%')))")
    Page<Teclado> findTeclados(@Param("nombre") String nombre, Pageable pageable);

    @Query(value = "SELECT new org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.TecladoDTO(" +
            "t.id, t.nombre, t.marca, t.fechaPublicacion, t.precio, t.stock, t.tipoProducto, t.image, " +
            "t.tipoSwitch, t.layout, t.conectividad, t.retroiluminacion) " +
            "FROM Teclado t",
            countQuery = "SELECT count(t) FROM Teclado t")
    Page<TecladoDTO> findAllTecladoDTOs(Pageable pageable);

    @Query("SELECT new org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.TecladoDTO(" +
            "t.id, t.nombre, t.marca, t.fechaPublicacion, t.precio, t.stock, t.tipoProducto, t.image, " +
            "t.tipoSwitch, t.layout, t.conectividad, t.retroiluminacion) " +
            "FROM Teclado t WHERE t.id = :id")
    Optional<TecladoDTO> findTecladoDTOById(@Param("id") Long id);

    default List<TecladoDTO> getHomeProductos() {
        return findAllTecladoDTOs(PageRequest.of(0, 5)).getContent();
    }
}
