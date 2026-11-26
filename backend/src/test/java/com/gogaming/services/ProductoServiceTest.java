package com.gogaming.services;

import com.gogaming.dtos.ProductoDTO;
import com.gogaming.dtos.RatonDTO;
import com.gogaming.entities.Producto;
import com.gogaming.entities.Raton;
import com.gogaming.entities.Teclado;
import com.gogaming.entities.TipoAgarre;
import com.gogaming.entities.TipoProducto;
import com.gogaming.repositories.ProductoRepository;
import com.gogaming.services.ProductoService.DatosProductoInvalidosException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductoServiceTest {

    private final ProductoRepository repo = mock(ProductoRepository.class);
    private final ProductoService service = new ProductoService(repo, JsonMapper.builder().build(),
            Validation.buildDefaultValidatorFactory().getValidator());

    private static Raton raton(Long id) {
        Raton r = new Raton(TipoAgarre.Claw, "120x60x38mm", "PAW3395", new BigDecimal("55"), "Optical");
        r.setId(id);
        r.setNombre("X2H");
        r.setMarca("Pulsar");
        r.setPrecio(new BigDecimal("99.99"));
        r.setStock(20);
        r.setTipoProducto(TipoProducto.Raton);
        r.setImage("https://img/x2h.png");
        return r;
    }

    private static Map<String, Object> datosRaton() {
        Map<String, Object> d = new HashMap<>();
        d.put("nombre", "Nuevo");
        d.put("marca", "Marca");
        d.put("precio", 59.9);
        d.put("stock", 5);
        d.put("image", "https://img/n.png");
        d.put("tipoAgarre", "Palm");
        d.put("medidas", "120x60x40mm");
        d.put("sensor", "PAW3950");
        d.put("peso", 60);
        d.put("switches", "Omron");
        d.put("tipoProducto", "Manga"); // lo que diga el cliente se ignora
        return d;
    }

    @Test
    void listarConvierteLasEntidadesAlDtoDeSuCategoria() {
        when(repo.buscarPorCategoria(eq(Raton.class), eq(null), any())).thenReturn(new PageImpl<>(List.of(raton(1L))));

        Page<ProductoDTO> pagina = service.listar(CategoriaProducto.RATON, "  ", PageRequest.of(0, 10));

        assertThat(pagina.getContent()).singleElement().isInstanceOfSatisfying(RatonDTO.class, dto -> {
            assertThat(dto.getNombre()).isEqualTo("X2H");
            assertThat(dto.getSensor()).isEqualTo("PAW3395");
            assertThat(dto.getTipoAgarre()).isEqualTo(TipoAgarre.Claw);
        });
    }

    @Test
    void elFiltroSeNormalizaYElOrdenSoloAdmiteCamposComunesConDesempatePorId() {
        when(repo.buscarPorCategoria(any(), any(), any())).thenReturn(Page.empty());
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);

        service.listar(CategoriaProducto.RATON, "  PuLsAr ", PageRequest.of(1, 5, Sort.by("precio").descending().and(Sort.by("peso"))));

        verify(repo).buscarPorCategoria(eq(Raton.class), eq("pulsar"), pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageable.getValue().getSort().toList())
                .extracting(Sort.Order::getProperty)
                .as("'peso' no es un campo común y se descarta; 'id' desempata")
                .containsExactly("precio", "id");
    }

    @Test
    void buscarEnOtraCategoriaNoDevuelveNada() {
        when(repo.findById(1L)).thenReturn(Optional.of(raton(1L)));

        assertThat(service.buscar(CategoriaProducto.RATON, 1L)).isPresent();
        assertThat(service.buscar(CategoriaProducto.TECLADO, 1L)).isEmpty();
    }

    @Test
    void crearFijaLaCategoriaSegunLaUrlYNoElCuerpo() {
        when(repo.save(any())).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            p.setId(50L);
            return p;
        });

        ProductoDTO creado = service.crear(CategoriaProducto.RATON, datosRaton());

        ArgumentCaptor<Producto> guardado = ArgumentCaptor.forClass(Producto.class);
        verify(repo).save(guardado.capture());
        assertThat(guardado.getValue()).isInstanceOf(Raton.class);
        assertThat(guardado.getValue().getTipoProducto()).isEqualTo(TipoProducto.Raton);
        assertThat(creado.getId()).isEqualTo(50L);
    }

    @Test
    void crearConDatosIncompletosDevuelveTodosLosErrores() {
        Map<String, Object> datos = datosRaton();
        datos.remove("sensor");
        datos.put("precio", -1);

        assertThatThrownBy(() -> service.crear(CategoriaProducto.RATON, datos))
                .isInstanceOfSatisfying(DatosProductoInvalidosException.class, e ->
                        assertThat(e.getErrores()).contains("Sensor es obligatorio", "El precio no puede ser negativo"));
        verify(repo, never()).save(any());
    }

    @Test
    void crearConTiposErroneosEsUnErrorDeFormato() {
        Map<String, Object> datos = datosRaton();
        datos.put("precio", "gratis");

        assertThatThrownBy(() -> service.crear(CategoriaProducto.RATON, datos))
                .isInstanceOf(DatosProductoInvalidosException.class);
    }

    @Test
    void actualizarUnProductoDeOtraCategoriaNoHaceNada() {
        Teclado teclado = new Teclado();
        teclado.setId(3L);
        when(repo.findById(3L)).thenReturn(Optional.of(teclado));

        assertThat(service.actualizar(CategoriaProducto.RATON, 3L, datosRaton())).isEmpty();
        verify(repo, never()).save(any());
    }

    @Test
    void eliminarDevuelveFalseSiNoExiste() {
        when(repo.findById(9L)).thenReturn(Optional.empty());

        assertThat(service.eliminar(CategoriaProducto.RATON, 9L)).isFalse();
        verify(repo, never()).delete(any());
    }

    @Test
    void cadaSlugDeLaUrlCorrespondeAUnaCategoria() {
        assertThat(CategoriaProducto.desdeSlug("auriculares")).contains(CategoriaProducto.AURICULARES);
        assertThat(CategoriaProducto.desdeSlug("patinete")).isEmpty();
        assertThat(CategoriaProducto.de(raton(1L))).isEqualTo(CategoriaProducto.RATON);
    }
}
