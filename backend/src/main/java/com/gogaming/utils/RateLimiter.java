package com.gogaming.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Límite de peticiones en memoria con ventana deslizante: como mucho {@code max} eventos por clave
 * en cada {@code ventana}. Suficiente para una sola instancia del backend; con varias haría falta Redis o similar.
 */
public class RateLimiter {

    private final int max;
    private final Duration ventana;
    private final Map<String, Deque<Instant>> eventos = new ConcurrentHashMap<>();

    public RateLimiter(int max, Duration ventana) {
        this.max = max;
        this.ventana = ventana;
    }

    /** Registra un evento si cabe en el límite. @return false si la clave ya lo ha superado. */
    public boolean intentar(String clave) {
        Instant ahora = Instant.now();
        Deque<Instant> tiempos = tiempos(clave);
        synchronized (tiempos) {
            purgar(tiempos, ahora);
            if (tiempos.size() >= max) {
                return false;
            }
            tiempos.addLast(ahora);
            return true;
        }
    }

    /** Registra un evento sin comprobar el límite (p. ej. un intento fallido). */
    public void registrar(String clave) {
        Deque<Instant> tiempos = tiempos(clave);
        synchronized (tiempos) {
            tiempos.addLast(Instant.now());
        }
    }

    /** ¿Ha llegado la clave al límite? (no registra nada) */
    public boolean bloqueado(String clave) {
        Deque<Instant> tiempos = eventos.get(clave);
        if (tiempos == null) return false;
        synchronized (tiempos) {
            purgar(tiempos, Instant.now());
            return tiempos.size() >= max;
        }
    }

    public void reiniciar(String clave) {
        eventos.remove(clave);
    }

    private Deque<Instant> tiempos(String clave) {
        // Se eliminan las claves sin eventos recientes, para que el mapa no crezca sin fin.
        if (eventos.size() > 10_000) {
            Instant ahora = Instant.now();
            eventos.entrySet().removeIf(e -> {
                synchronized (e.getValue()) {
                    purgar(e.getValue(), ahora);
                    return e.getValue().isEmpty();
                }
            });
        }
        return eventos.computeIfAbsent(clave, k -> new ArrayDeque<>());
    }

    private void purgar(Deque<Instant> tiempos, Instant ahora) {
        Instant limite = ahora.minus(ventana);
        while (!tiempos.isEmpty() && tiempos.peekFirst().isBefore(limite)) {
            tiempos.pollFirst();
        }
    }
}
