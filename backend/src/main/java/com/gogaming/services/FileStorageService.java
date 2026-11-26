package com.gogaming.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Imágenes de perfil de los usuarios, guardadas en UPLOAD_PATH.
 * Solo se aceptan JPEG, PNG, WebP y GIF (por extensión y por los primeros bytes del archivo):
 * un .html o un .svg servido desde el dominio de la API permitiría ataques XSS.
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    /** Extensión permitida → tipo MIME con el que se sirve. */
    private static final Map<String, String> FORMATOS = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp",
            "gif", "image/gif");

    private final Path uploadDir;

    public FileStorageService(@Value("${UPLOAD_PATH}") String uploadPath) {
        this.uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
    }

    /**
     * Guarda la imagen con un nombre aleatorio y devuelve ese nombre.
     * @throws IllegalArgumentException si no es una imagen de un formato permitido
     * @throws IOException si falla la escritura
     */
    public String saveFile(MultipartFile file) throws IOException {
        String extension = extension(file.getOriginalFilename());
        if (!FORMATOS.containsKey(extension) || !esImagen(file)) {
            throw new IllegalArgumentException("Formato de imagen no permitido (usa JPG, PNG, WebP o GIF).");
        }
        String nombre = UUID.randomUUID() + "." + extension;
        Files.createDirectories(uploadDir);
        Files.write(uploadDir.resolve(nombre), file.getBytes());
        logger.info("Imagen {} guardada.", nombre);
        return nombre;
    }

    public void deleteFile(String fileName) {
        Path ruta = resolver(fileName);
        if (ruta == null) return;
        try {
            Files.deleteIfExists(ruta);
        } catch (IOException e) {
            logger.warn("No se pudo eliminar la imagen {}: {}", fileName, e.getMessage());
        }
    }

    public ResponseEntity<?> readImage(String fileName) {
        Path ruta = resolver(fileName);
        String tipo = FORMATOS.get(extension(fileName));
        if (ruta == null || tipo == null || !Files.isReadable(ruta)) {
            return ResponseEntity.notFound().build();
        }
        try {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, tipo)
                    .header("X-Content-Type-Options", "nosniff")
                    .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                    .body(new UrlResource(ruta.toUri()));
        } catch (IOException e) {
            logger.error("Error al servir la imagen {}: {}", fileName, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /** Ruta dentro de la carpeta de subidas, o null si el nombre intenta salir de ella. */
    private Path resolver(String fileName) {
        if (fileName == null || fileName.isBlank()) return null;
        Path ruta = uploadDir.resolve(fileName).normalize();
        return ruta.startsWith(uploadDir) && !ruta.equals(uploadDir) ? ruta : null;
    }

    private static String extension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "";
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    /** Comprueba la firma ("magic bytes") de los formatos permitidos. */
    private static boolean esImagen(MultipartFile file) throws IOException {
        byte[] c;
        try (InputStream in = file.getInputStream()) {
            c = in.readNBytes(12);
        }
        if (c.length < 12) return false;
        boolean jpeg = (c[0] & 0xFF) == 0xFF && (c[1] & 0xFF) == 0xD8 && (c[2] & 0xFF) == 0xFF;
        boolean png = (c[0] & 0xFF) == 0x89 && c[1] == 'P' && c[2] == 'N' && c[3] == 'G';
        boolean gif = c[0] == 'G' && c[1] == 'I' && c[2] == 'F' && c[3] == '8';
        boolean webp = c[0] == 'R' && c[1] == 'I' && c[2] == 'F' && c[3] == 'F'
                && Arrays.equals(Arrays.copyOfRange(c, 8, 12), "WEBP".getBytes());
        return jpeg || png || gif || webp;
    }
}
