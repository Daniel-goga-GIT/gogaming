package com.gogaming.repositories;

import com.gogaming.entities.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Productos de una categoría (subclase de {@link Producto}), con filtro opcional
     * por nombre o marca. {@code texto} debe llegar ya en minúsculas o a null.
     */
    @Query("SELECT p FROM Producto p WHERE TYPE(p) = :categoria " +
            "AND (:texto IS NULL OR LOWER(p.nombre) LIKE CONCAT('%', :texto, '%') " +
            "OR LOWER(p.marca) LIKE CONCAT('%', :texto, '%'))")
    Page<Producto> buscarPorCategoria(@Param("categoria") Class<? extends Producto> categoria,
                                      @Param("texto") String texto,
                                      Pageable pageable);
}
