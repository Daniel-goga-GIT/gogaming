package com.gogaming.repositories;

import com.gogaming.entities.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Long> {

    List<Resena> findByProductoIdOrderByFechaCreacionDesc(Long productId);

    List<Resena> findByUsuarioIdOrderByFechaCreacionDesc(Long userId);

    @Query("SELECT AVG(r.calificacion) FROM Resena r WHERE r.producto.id = :productId")
    Double findAverageByProductoId(@Param("productId") Long productId);

    long countByProductoIdAndCalificacion(Long productId, int calificacion);
}
