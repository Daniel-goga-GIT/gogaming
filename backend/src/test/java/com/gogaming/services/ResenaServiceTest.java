package com.gogaming.services;

import com.gogaming.dtos.ProductRatingSummaryDTO;
import com.gogaming.dtos.ResenaCreateDTO;
import com.gogaming.entities.Raton;
import com.gogaming.entities.User;
import com.gogaming.repositories.ProductoRepository;
import com.gogaming.repositories.ResenaRepository;
import com.gogaming.repositories.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResenaServiceTest {

    private final ResenaRepository resenas = mock(ResenaRepository.class);
    private final ProductoRepository productos = mock(ProductoRepository.class);
    private final UserRepository usuarios = mock(UserRepository.class);
    private final ResenaService service = new ResenaService(resenas, productos, usuarios);

    @Test
    void calculaMediaYRecuentosDeVariosProductosConUnaConsulta() {
        when(resenas.contarPorProductoYNota(any())).thenReturn(List.of(
                new Object[]{1L, 5, 3L},
                new Object[]{1L, 4, 1L},
                new Object[]{2L, 1, 2L}));

        Map<Long, ProductRatingSummaryDTO> resumen = service.getSummaries(List.of(1L, 2L, 3L));

        assertThat(resumen.get(1L).getTotalReviews()).isEqualTo(4);
        assertThat(resumen.get(1L).getAverageRating()).isEqualTo(4.8); // (5*3 + 4) / 4 = 4.75 → 4.8
        assertThat(resumen.get(1L).getFiveStars()).isEqualTo(3);
        assertThat(resumen.get(2L).getAverageRating()).isEqualTo(1.0);
        assertThat(resumen.get(3L).getTotalReviews()).as("sin reseñas → ceros").isZero();
        assertThat(resumen.get(3L).getAverageRating()).isZero();
    }

    @Test
    void sinIdsNoConsultaLaBaseDeDatos() {
        assertThat(service.getSummaries(List.of())).isEmpty();
        verify(resenas, never()).contarPorProductoYNota(any());
    }

    @Test
    void unUsuarioNoPuedeValorarDosVecesElMismoProducto() {
        when(usuarios.findById(1L)).thenReturn(Optional.of(new User()));
        when(productos.findById(2L)).thenReturn(Optional.of(new Raton()));
        when(resenas.existsByUsuarioIdAndProductoId(1L, 2L)).thenReturn(true);

        ResenaCreateDTO body = new ResenaCreateDTO();
        body.setCalificacion(5);
        body.setContenido("Otra vez");

        assertThatThrownBy(() -> service.createReview(1L, 2L, body)).isInstanceOf(ResenaService.ResenaDuplicadaException.class);
        verify(resenas, never()).save(any());
    }
}
