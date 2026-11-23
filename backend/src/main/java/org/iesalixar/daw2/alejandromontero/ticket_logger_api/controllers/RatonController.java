package org.iesalixar.daw2.alejandromontero.ticket_logger_api.controllers;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.RatonDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.services.RatonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/productos/raton")
public class RatonController {

    @Autowired
    private RatonService ratonService;

    @GetMapping
    public ResponseEntity<Page<RatonDTO>> getAllRatones(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<RatonDTO> ratones = ratonService.getAllRatones(nombre, pageable);
        return ResponseEntity.ok(ratones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RatonDTO> getRatonById(@PathVariable Long id) {
        RatonDTO raton = ratonService.getRatonById(id);
        if (raton == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(raton);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RatonDTO> createRaton(@RequestBody RatonDTO ratonDTO) {
        RatonDTO createdRaton = ratonService.createRaton(ratonDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRaton);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RatonDTO> updateRaton(@PathVariable Long id, @RequestBody RatonDTO ratonDTO) {
        RatonDTO updatedRaton = ratonService.updateRaton(id, ratonDTO);
        if (updatedRaton == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedRaton);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRaton(@PathVariable Long id) {
        ratonService.deleteRaton(id);
        return ResponseEntity.noContent().build();
    }
}
