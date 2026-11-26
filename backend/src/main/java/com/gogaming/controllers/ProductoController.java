package com.gogaming.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.gogaming.dtos.ProductoDTO;
import com.gogaming.services.CategoriaProducto;
import com.gogaming.services.ProductoService;
import com.gogaming.services.ProductoService.DatosProductoInvalidosException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Catálogo de productos. Una sola API para las seis categorías:
 * {@code /api/v1/productos/{categoria}} con categoria = raton, teclado, skate, alfombrilla, auriculares o manga.
 * La lectura es pública; crear, editar y borrar es solo para administradores.
 */
@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "Catálogo de periféricos y mangas")
public class ProductoController {

    private static final String CATEGORIA = "/{categoria:[a-z]+}";
    private static final String CATEGORIA_ID = CATEGORIA + "/{id:\\d+}";

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Operation(summary = "Productos destacados de la portada")
    @GetMapping("/home")
    public List<ProductoDTO> portada() {
        return productoService.portada();
    }

    @Operation(summary = "Un producto por su id, sea de la categoría que sea")
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ProductoDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.of(productoService.buscar(id));
    }

    @Operation(summary = "Productos de una categoría, paginados y con filtro opcional por nombre o marca")
    @GetMapping(CATEGORIA)
    public Page<ProductoDTO> listar(@Parameter(example = "raton") @PathVariable String categoria,
                                    @RequestParam(required = false) String nombre,
                                    Pageable pageable) {
        return productoService.listar(categoria(categoria), nombre, pageable);
    }

    @Operation(summary = "Un producto de una categoría")
    @GetMapping(CATEGORIA_ID)
    public ResponseEntity<ProductoDTO> buscar(@PathVariable String categoria, @PathVariable Long id) {
        return ResponseEntity.of(productoService.buscar(categoria(categoria), id));
    }

    @Operation(summary = "Crear un producto (admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(CATEGORIA)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoDTO> crear(@PathVariable String categoria, @RequestBody Map<String, Object> datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(categoria(categoria), datos));
    }

    @Operation(summary = "Editar un producto (admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping(CATEGORIA_ID)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoDTO> actualizar(@PathVariable String categoria, @PathVariable Long id,
                                                  @RequestBody Map<String, Object> datos) {
        return ResponseEntity.of(productoService.actualizar(categoria(categoria), id, datos));
    }

    @Operation(summary = "Borrar un producto (admin)")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(CATEGORIA_ID)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable String categoria, @PathVariable Long id) {
        return productoService.eliminar(categoria(categoria), id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @ExceptionHandler(DatosProductoInvalidosException.class)
    public ResponseEntity<Map<String, Object>> datosInvalidos(DatosProductoInvalidosException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "errors", e.getErrores()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> enUso(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "message", "No se puede borrar: el producto aparece en pedidos o reseñas."));
    }

    private static CategoriaProducto categoria(String slug) {
        return CategoriaProducto.desdeSlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría desconocida: " + slug));
    }
}
