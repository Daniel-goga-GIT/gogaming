package org.iesalixar.daw2.alejandromontero.ticket_logger_api.controllers;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.MangaDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.services.MangaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/productos/manga")
public class MangaController {

    @Autowired
    private MangaService mangaService;

    @GetMapping
    public ResponseEntity<Page<MangaDTO>> getAllMangas(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        Page<MangaDTO> mangas = mangaService.getAllMangas(nombre, pageable);
        return ResponseEntity.ok(mangas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MangaDTO> getMangaById(@PathVariable Long id) {
        MangaDTO manga = mangaService.getMangaById(id);
        if (manga == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(manga);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MangaDTO> createManga(@RequestBody MangaDTO mangaDTO) {
        MangaDTO createdManga = mangaService.createManga(mangaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdManga);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MangaDTO> updateManga(@PathVariable Long id, @RequestBody MangaDTO mangaDTO) {
        MangaDTO updatedManga = mangaService.updateManga(id, mangaDTO);
        if (updatedManga == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedManga);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteManga(@PathVariable Long id) {
        mangaService.deleteManga(id);
        return ResponseEntity.noContent().build();
    }
}
