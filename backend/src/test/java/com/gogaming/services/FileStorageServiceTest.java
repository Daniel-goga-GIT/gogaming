package com.gogaming.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    /** PNG real de 1x1 píxel. */
    private static final byte[] PNG = HexFormat.of().parseHex(
            "89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000D4944415478DA63F8FFFF3F0005FE02FEA7D6A4E20000000049454E44AE426082");

    @TempDir
    Path dir;

    private FileStorageService service() {
        return new FileStorageService(dir.toString());
    }

    @Test
    void guardaUnaImagenValidaConNombreAleatorioYLaSirveConSuTipo() throws Exception {
        String nombre = service().saveFile(new MockMultipartFile("image", "Mi Foto.PNG", "image/png", PNG));

        assertThat(nombre).endsWith(".png").doesNotContain("Mi Foto");
        assertThat(Files.readAllBytes(dir.resolve(nombre))).isEqualTo(PNG);

        ResponseEntity<?> respuesta = service().readImage(nombre);
        assertThat(respuesta.getStatusCode().value()).isEqualTo(200);
        assertThat(respuesta.getHeaders().getContentType()).hasToString("image/png");
        assertThat(respuesta.getHeaders().getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
    }

    @Test
    void rechazaExtensionesQueNoSonImagenes() {
        var html = new MockMultipartFile("image", "xss.html", "text/html", "<script>alert(1)</script>".getBytes());
        var svg = new MockMultipartFile("image", "logo.svg", "image/svg+xml", "<svg onload=alert(1)>".getBytes());

        assertThatThrownBy(() -> service().saveFile(html)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service().saveFile(svg)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaUnArchivoDisfrazadoDePng() {
        var falso = new MockMultipartFile("image", "foto.png", "image/png", "<html>no soy un png</html>".getBytes());

        assertThatThrownBy(() -> service().saveFile(falso)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void noSirveArchivosFueraDeLaCarpetaNiConExtensionesNoPermitidas() throws Exception {
        Files.writeString(dir.getParent().resolve("secreto.png"), "x");
        Files.writeString(dir.resolve("notas.txt"), "x");

        assertThat(service().readImage("../secreto.png").getStatusCode().value()).isEqualTo(404);
        assertThat(service().readImage("notas.txt").getStatusCode().value()).isEqualTo(404);
        assertThat(service().readImage("no-existe.png").getStatusCode().value()).isEqualTo(404);
    }
}
