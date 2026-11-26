package com.gogaming.services;

import com.gogaming.dtos.ProductRatingSummaryDTO;
import com.gogaming.dtos.ProductReviewsResponseDTO;
import com.gogaming.dtos.ResenaCreateDTO;
import com.gogaming.dtos.ResenaResponseDTO;
import com.gogaming.entities.Producto;
import com.gogaming.entities.Resena;
import com.gogaming.entities.User;
import com.gogaming.repositories.ProductoRepository;
import com.gogaming.repositories.ResenaRepository;
import com.gogaming.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final ProductoRepository productoRepository;
    private final UserRepository userRepository;

    public ResenaService(ResenaRepository resenaRepository, ProductoRepository productoRepository, UserRepository userRepository) {
        this.resenaRepository = resenaRepository;
        this.productoRepository = productoRepository;
        this.userRepository = userRepository;
    }

    public ProductReviewsResponseDTO getProductReviews(Long productId) {
        ensureProductExists(productId);

        List<Resena> reviewsEntity = resenaRepository.findByProductoIdOrderByFechaCreacionDesc(productId);

        List<ResenaResponseDTO> reviews = reviewsEntity.stream().map(this::toResponse).toList();

        return new ProductReviewsResponseDTO(getProductSummary(productId),reviews);
    }

    public List<ResenaResponseDTO> getUserReviews(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        return resenaRepository.findByUsuarioIdOrderByFechaCreacionDesc(userId).stream().map(this::toResponse).toList();
    }

    public ProductRatingSummaryDTO getProductSummary(Long productId) {
        ensureProductExists(productId);
        return getSummaries(List.of(productId)).get(productId);
    }

    /**
     * Resumen de valoraciones de varios productos con una sola consulta.
     * Los productos sin reseñas (o que no existen) aparecen con todo a cero.
     */
    public Map<Long, ProductRatingSummaryDTO> getSummaries(Collection<Long> productIds) {
        Map<Long, long[]> porNota = new LinkedHashMap<>();
        productIds.forEach(id -> porNota.put(id, new long[6]));
        if (!productIds.isEmpty()) {
            for (Object[] fila : resenaRepository.contarPorProductoYNota(productIds)) {
                long[] cuentas = porNota.get((Long) fila[0]);
                int nota = ((Number) fila[1]).intValue();
                if (cuentas != null && nota >= 1 && nota <= 5) {
                    cuentas[nota] = (Long) fila[2];
                }
            }
        }
        Map<Long, ProductRatingSummaryDTO> resumen = new LinkedHashMap<>();
        porNota.forEach((id, c) -> {
            long total = c[1] + c[2] + c[3] + c[4] + c[5];
            double media = total == 0 ? 0.0 : BigDecimal.valueOf((c[1] + 2 * c[2] + 3 * c[3] + 4 * c[4] + 5 * c[5]) / (double) total)
                    .setScale(1, RoundingMode.HALF_UP).doubleValue();
            resumen.put(id, new ProductRatingSummaryDTO(id, media, total, c[1], c[2], c[3], c[4], c[5]));
        });
        return resumen;
    }

    public ResenaResponseDTO createReview(Long userId, Long productId, ResenaCreateDTO body) {
        User usuario = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Producto producto = productoRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        if (resenaRepository.existsByUsuarioIdAndProductoId(userId, productId)) {
            throw new ResenaDuplicadaException();
        }

        Resena resena = new Resena();
        resena.setUsuario(usuario);
        resena.setProducto(producto);
        resena.setCalificacion(body.getCalificacion());
        resena.setContenido(body.getContenido().trim());

        return toResponse(resenaRepository.save(resena));
    }

    private void ensureProductExists(Long productId) {
        if (!productoRepository.existsById(productId)) {
            throw new IllegalArgumentException("Producto no encontrado");
        }
    }

    private ResenaResponseDTO toResponse(Resena resena) {
        String userName = (resena.getUsuario().getFirstName() + " " + resena.getUsuario().getLastName()).trim();

        return new ResenaResponseDTO(
                resena.getId(),
                resena.getProducto().getId(),
                resena.getUsuario().getId(),
                userName,
                resena.getCalificacion(),
                resena.getContenido(),
                resena.getFechaCreacion(),
                resena.getFechaActualizacion()
        );
    }

    /** El usuario ya ha valorado ese producto. */
    public static class ResenaDuplicadaException extends RuntimeException {
        public ResenaDuplicadaException() {
            super("Ya has valorado este producto.");
        }
    }
}
