package org.iesalixar.daw2.alejandromontero.ticket_logger_api.controllers;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.SkateDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.services.SkateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/productos/skate")
public class SkateController {

    @Autowired
    private SkateService skateService;

    @GetMapping
    public ResponseEntity<Page<SkateDTO>> getAllSkates(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<SkateDTO> skates = skateService.getAllSkates(nombre, pageable);
        return ResponseEntity.ok(skates);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SkateDTO> getSkateById(@PathVariable Long id) {
        SkateDTO skate = skateService.getSkateById(id);
        if (skate == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(skate);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SkateDTO> createSkate(@RequestBody SkateDTO skateDTO) {
        SkateDTO createdSkate = skateService.createSkate(skateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSkate);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SkateDTO> updateSkate(@PathVariable Long id, @RequestBody SkateDTO skateDTO) {
        SkateDTO updatedSkate = skateService.updateSkate(id, skateDTO);
        if (updatedSkate == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedSkate);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSkate(@PathVariable Long id) {
        skateService.deleteSkate(id);
        return ResponseEntity.noContent().build();
    }
}
