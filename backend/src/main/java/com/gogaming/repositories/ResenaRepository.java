package com.gogaming.repositories;

import com.gogaming.entities.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Long> {

    List<Resena> findByProductoIdOrderByFechaCreacionDesc(Long productId);

    List<Resena> findByUsuarioIdOrderByFechaCreacionDesc(Long userId);

    boolean existsByUsuarioIdAndProductoId(Long userId, Long productId);

    /** Número de reseñas por producto y por nota (1-5): filas [productoId, calificacion, total]. */
    @Query("SELECT r.producto.id, r.calificacion, COUNT(r) FROM Resena r " +
            "WHERE r.producto.id IN :ids GROUP BY r.producto.id, r.calificacion")
    List<Object[]> contarPorProductoYNota(@Param("ids") Collection<Long> ids);
}
