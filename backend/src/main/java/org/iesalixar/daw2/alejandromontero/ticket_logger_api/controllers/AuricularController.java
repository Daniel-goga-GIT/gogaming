package org.iesalixar.daw2.alejandromontero.ticket_logger_api.controllers;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.AuricularesDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.services.AuricularesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/productos/auriculares")
public class AuricularController {

    @Autowired
    private AuricularesService auricularesService;

    @GetMapping
    public ResponseEntity<Page<AuricularesDTO>> getAllAuriculares(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<AuricularesDTO> auriculares = auricularesService.getAllAuriculares(nombre, pageable);
        return ResponseEntity.ok(auriculares);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuricularesDTO> getAuricularesById(@PathVariable Long id) {
        AuricularesDTO auriculares = auricularesService.getAuricularesById(id);
        if (auriculares == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(auriculares);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuricularesDTO> createAuriculares(@RequestBody AuricularesDTO auricularesDTO) {
        AuricularesDTO createdAuriculares = auricularesService.createAuriculares(auricularesDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAuriculares);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuricularesDTO> updateAuriculares(@PathVariable Long id, @RequestBody AuricularesDTO auricularesDTO) {
        AuricularesDTO updatedAuriculares = auricularesService.updateAuriculares(id, auricularesDTO);
        if (updatedAuriculares == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedAuriculares);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAuriculares(@PathVariable Long id) {
        auricularesService.deleteAuriculares(id);
        return ResponseEntity.noContent().build();
    }
}
