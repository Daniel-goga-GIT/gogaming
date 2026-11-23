package org.iesalixar.daw2.alejandromontero.ticket_logger_api.services;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.ProductRatingSummaryDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.ProductReviewsResponseDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.ResenaCreateDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.ResenaResponseDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Producto;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Resena;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.User;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories.ProductoRepository;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories.ResenaRepository;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

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

        Double average = resenaRepository.findAverageByProductoId(productId);
        double normalizedAverage = average == null ? 0.0 : BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP).doubleValue();

        long one   = resenaRepository.countByProductoIdAndCalificacion(productId, 1);
        long two   = resenaRepository.countByProductoIdAndCalificacion(productId, 2);
        long three = resenaRepository.countByProductoIdAndCalificacion(productId, 3);
        long four  = resenaRepository.countByProductoIdAndCalificacion(productId, 4);
        long five  = resenaRepository.countByProductoIdAndCalificacion(productId, 5);
        long total = one + two + three + four + five;

        return new ProductRatingSummaryDTO(productId, normalizedAverage, total, one, two, three, four, five);
    }

    public ResenaResponseDTO createReview(Long userId, Long productId, ResenaCreateDTO body) {
        User usuario = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Producto producto = productoRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

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
}
