package com.gogaming.controllers;

import jakarta.validation.Valid;
import com.gogaming.dtos.ProductRatingSummaryDTO;
import com.gogaming.dtos.ProductReviewsResponseDTO;
import com.gogaming.dtos.ResenaCreateDTO;
import com.gogaming.dtos.ResenaResponseDTO;
import com.gogaming.services.ResenaService;
import com.gogaming.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/productos/raton/{productId}/resenas")
public class ResenaController {

    private final ResenaService resenaService;
    private final JwtUtil jwtUtil;

    public ResenaController(ResenaService resenaService, JwtUtil jwtUtil) {
        this.resenaService = resenaService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<?> getReviews(@PathVariable Long productId) {
        try {
            ProductReviewsResponseDTO response = resenaService.getProductReviews(productId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cargar valoraciones");
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(@PathVariable Long productId) {
        try {
            ProductRatingSummaryDTO response = resenaService.getProductSummary(productId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cargar resumen de valoraciones");
        }
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping
    public ResponseEntity<?> createReview(
            @PathVariable Long productId,
            @RequestHeader("Authorization") String tokenHeader,
            @Valid @RequestBody ResenaCreateDTO body
    ) {
        try {
            Long userId = extractUserId(tokenHeader);
            ResenaResponseDTO created = resenaService.createReview(userId, productId, body);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear reseña");
        }
    }

    private Long extractUserId(String tokenHeader) {
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token de autorizacion invalido");
        }

        String token = tokenHeader.replace("Bearer ", "");
        Long userId = jwtUtil.extractClaim(token, claims -> claims.get("id", Long.class));

        if (userId == null) {
            throw new IllegalArgumentException("No se pudo obtener el usuario autenticado");
        }

        return userId;
    }
}
