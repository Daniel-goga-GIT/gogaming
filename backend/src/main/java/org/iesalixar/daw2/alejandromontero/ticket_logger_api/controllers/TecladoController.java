package org.iesalixar.daw2.alejandromontero.ticket_logger_api.controllers;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.TecladoDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.services.TecladoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/productos/teclado")
public class TecladoController {

    @Autowired
    private TecladoService tecladoService;

    @GetMapping
    public ResponseEntity<Page<TecladoDTO>> getAllTeclados(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<TecladoDTO> teclados = tecladoService.getAllTeclados(nombre, pageable);
        return ResponseEntity.ok(teclados);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TecladoDTO> getTecladoById(@PathVariable Long id) {
        TecladoDTO teclado = tecladoService.getTecladoById(id);
        if (teclado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(teclado);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TecladoDTO> createTeclado(@RequestBody TecladoDTO tecladoDTO) {
        TecladoDTO createdTeclado = tecladoService.createTeclado(tecladoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTeclado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TecladoDTO> updateTeclado(@PathVariable Long id, @RequestBody TecladoDTO tecladoDTO) {
        TecladoDTO updatedTeclado = tecladoService.updateTeclado(id, tecladoDTO);
        if (updatedTeclado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedTeclado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTeclado(@PathVariable Long id) {
        tecladoService.deleteTeclado(id);
        return ResponseEntity.noContent().build();
    }
}
