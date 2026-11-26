package com.gogaming.utils;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterTest {

    @Test
    void dejaPasarHastaElMaximoYLuegoBloquea() {
        RateLimiter limiter = new RateLimiter(3, Duration.ofMinutes(1));

        assertThat(limiter.intentar("ip")).isTrue();
        assertThat(limiter.intentar("ip")).isTrue();
        assertThat(limiter.intentar("ip")).isTrue();
        assertThat(limiter.intentar("ip")).isFalse();
        assertThat(limiter.intentar("otra-ip")).as("cada clave tiene su propio límite").isTrue();
    }

    @Test
    void losEventosCaducanAlSalirDeLaVentana() throws InterruptedException {
        RateLimiter limiter = new RateLimiter(1, Duration.ofMillis(100));

        assertThat(limiter.intentar("ip")).isTrue();
        assertThat(limiter.intentar("ip")).isFalse();
        Thread.sleep(150);
        assertThat(limiter.intentar("ip")).isTrue();
    }

    @Test
    void registrarYBloqueadoPermitenContarSoloLosFallos() {
        RateLimiter fallos = new RateLimiter(2, Duration.ofMinutes(1));

        assertThat(fallos.bloqueado("cuenta")).isFalse();
        fallos.registrar("cuenta");
        fallos.registrar("cuenta");
        assertThat(fallos.bloqueado("cuenta")).isTrue();

        fallos.reiniciar("cuenta");
        assertThat(fallos.bloqueado("cuenta")).isFalse();
    }
}
