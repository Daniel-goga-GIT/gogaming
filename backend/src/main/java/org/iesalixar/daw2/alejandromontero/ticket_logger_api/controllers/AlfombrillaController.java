package org.iesalixar.daw2.alejandromontero.ticket_logger_api.controllers;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.AlfombrillaDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.services.AlfombrillaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/productos/alfombrilla")
public class AlfombrillaController {

    @Autowired
    private AlfombrillaService alfombrillaService;

    @GetMapping
    public ResponseEntity<Page<AlfombrillaDTO>> getAllAlfombrillas(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<AlfombrillaDTO> alfombrillas = alfombrillaService.getAllAlfombrillas(nombre, pageable);
        return ResponseEntity.ok(alfombrillas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlfombrillaDTO> getAlfombrillaById(@PathVariable Long id) {
        AlfombrillaDTO alfombrilla = alfombrillaService.getAlfombrillaById(id);
        if (alfombrilla == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(alfombrilla);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlfombrillaDTO> createAlfombrilla(@RequestBody AlfombrillaDTO alfombrillaDTO) {
        AlfombrillaDTO createdAlfombrilla = alfombrillaService.createAlfombrilla(alfombrillaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAlfombrilla);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlfombrillaDTO> updateAlfombrilla(@PathVariable Long id, @RequestBody AlfombrillaDTO alfombrillaDTO) {
        AlfombrillaDTO updatedAlfombrilla = alfombrillaService.updateAlfombrilla(id, alfombrillaDTO);
        if (updatedAlfombrilla == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedAlfombrilla);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAlfombrilla(@PathVariable Long id) {
        alfombrillaService.deleteAlfombrilla(id);
        return ResponseEntity.noContent().build();
    }
}
