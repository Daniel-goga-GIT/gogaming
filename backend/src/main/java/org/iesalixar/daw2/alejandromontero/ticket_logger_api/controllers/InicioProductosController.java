package org.iesalixar.daw2.alejandromontero.ticket_logger_api.controllers;

import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.RatonDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Producto;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories.ProductoRepository;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories.RatonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
public class InicioProductosController {

    @Autowired
    private RatonRepository ratonRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping("/home")
    public ResponseEntity<List<RatonDTO>> getHomeProductos() {
        List<RatonDTO> raton = ratonRepository.getHomeProductos();
        return ResponseEntity.ok().body(raton);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> getProductoById(@PathVariable Long id) {
        return productoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
