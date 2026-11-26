package org.iesalixar.daw2.alejandromontero.ticket_logger_api.services;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.dtos.ProductoDTO;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.entities.Producto;
import org.iesalixar.daw2.alejandromontero.ticket_logger_api.repositories.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Lógica común a todas las categorías del catálogo (listar, ver, crear, editar y borrar).
 * La conversión entre entidad y DTO la hace Jackson: los DTO tienen los mismos campos que las entidades.
 */
@Service
@Transactional(readOnly = true)
public class ProductoService {

    /** Solo se puede ordenar por campos de la tabla común {@code productos}. */
    private static final Set<String> CAMPOS_ORDENABLES = Set.of("id", "nombre", "marca", "precio", "stock", "fechaPublicacion");
    private static final int PRODUCTOS_PORTADA = 5;

    private final ProductoRepository productoRepository;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public ProductoService(ProductoRepository productoRepository, ObjectMapper objectMapper, Validator validator) {
        this.productoRepository = productoRepository;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    public Page<ProductoDTO> listar(CategoriaProducto categoria, String texto, Pageable pageable) {
        String filtro = (texto == null || texto.isBlank()) ? null : texto.trim().toLowerCase(Locale.ROOT);
        return productoRepository.buscarPorCategoria(categoria.getEntidad(), filtro, ordenSeguro(pageable))
                .map(this::toDTO);
    }

    /** Productos destacados de la portada: los primeros ratones del catálogo. */
    public List<ProductoDTO> portada() {
        return listar(CategoriaProducto.RATON, null, PageRequest.of(0, PRODUCTOS_PORTADA, Sort.by("id"))).getContent();
    }

    public Optional<ProductoDTO> buscar(Long id) {
        return productoRepository.findById(id).map(this::toDTO);
    }

    public Optional<ProductoDTO> buscar(CategoriaProducto categoria, Long id) {
        return buscarEntidad(categoria, id).map(this::toDTO);
    }

    @Transactional
    public ProductoDTO crear(CategoriaProducto categoria, Map<String, Object> datos) {
        Producto producto = toEntidad(categoria, datos);
        producto.setId(null);
        return toDTO(productoRepository.save(producto));
    }

    @Transactional
    public Optional<ProductoDTO> actualizar(CategoriaProducto categoria, Long id, Map<String, Object> datos) {
        if (buscarEntidad(categoria, id).isEmpty()) {
            return Optional.empty();
        }
        Producto producto = toEntidad(categoria, datos);
        producto.setId(id);
        return Optional.of(toDTO(productoRepository.save(producto)));
    }

    /** @return false si el producto no existe en esa categoría. */
    @Transactional
    public boolean eliminar(CategoriaProducto categoria, Long id) {
        Optional<Producto> producto = buscarEntidad(categoria, id);
        producto.ifPresent(p -> {
            productoRepository.delete(p);
            productoRepository.flush(); // para que un fallo de clave ajena salte aquí y no al cerrar la transacción
        });
        return producto.isPresent();
    }

    private Optional<Producto> buscarEntidad(CategoriaProducto categoria, Long id) {
        return productoRepository.findById(id).filter(categoria.getEntidad()::isInstance);
    }

    private ProductoDTO toDTO(Producto producto) {
        return objectMapper.convertValue(producto, CategoriaProducto.de(producto).getDto());
    }

    /** Convierte el JSON recibido en el DTO de la categoría, lo valida y lo pasa a entidad. */
    private Producto toEntidad(CategoriaProducto categoria, Map<String, Object> datos) {
        ProductoDTO dto;
        try {
            dto = objectMapper.convertValue(datos, categoria.getDto());
        } catch (IllegalArgumentException | JacksonException e) {
            throw new DatosProductoInvalidosException(List.of("Formato de datos no válido"));
        }
        Set<ConstraintViolation<ProductoDTO>> errores = validator.validate(dto);
        if (!errores.isEmpty()) {
            throw new DatosProductoInvalidosException(errores.stream().map(ConstraintViolation::getMessage).sorted().toList());
        }
        dto.setTipoProducto(categoria.getTipo());
        return objectMapper.convertValue(dto, categoria.getEntidad());
    }

    private static Pageable ordenSeguro(Pageable pageable) {
        if (pageable.isUnpaged()) {
            return pageable;
        }
        List<Sort.Order> ordenes = pageable.getSort().stream()
                .filter(o -> CAMPOS_ORDENABLES.contains(o.getProperty()))
                .toList();
        Sort orden = Sort.by(ordenes);
        if (orden.getOrderFor("id") == null) {
            orden = orden.and(Sort.by("id")); // desempate: la paginación no repite ni salta productos
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), orden);
    }

    /** Los datos de un producto no superan la validación. */
    public static class DatosProductoInvalidosException extends RuntimeException {
        private final List<String> errores;

        public DatosProductoInvalidosException(List<String> errores) {
            super(String.join(". ", errores));
            this.errores = errores;
        }

        public List<String> getErrores() {
            return errores;
        }
    }
}
