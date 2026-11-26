package com.gogaming.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class DotenvConfig {
    // Se crea un logger para la clase utilizando SLF4J para registrar eventos importantes y posibles errores
    private static final Logger logger =
            LoggerFactory.getLogger(DotenvConfig.class);
    // Bloque estático para inicializar y cargar las variables de entorno del archivo .env
    static {
        try {
            // Registrar en el log un mensaje informativo indicando que el proceso de carga del archivo .env ha comenzado
            logger.info("Loading environment variables from .env file...");
            // Intenta cargar el .env del backend de forma explícita para evitar depender del cwd.
            Dotenv dotenv = loadBackendDotenv();
            // Iterar sobre todas las entradas (clave-valor) del archivo .env y establecerlas como propiedades del sistema
            dotenv.entries().forEach(entry -> {
                // Establecer cada variable de entorno como una propiedad del sistema
                System.setProperty(entry.getKey(), entry.getValue());
                // Registrar en el log el valor de cada variable cargada para fines de depuración (nivel DEBUG)
                logger.debug("Set system property: {} = {}", entry.getKey(),
                        entry.getValue());
            });
            // Registrar en el log un mensaje informativo indicando que el archivo .env se cargó correctamente
            logger.info(".env file loaded successfully.");
        } catch (Exception e) {
            // Si ocurre alguna excepción durante el proceso de carga del archivo.env, se captura y se registra un mensaje de error
            logger.error("Failed to load .env file", e);
        }
    }

    private static Dotenv loadBackendDotenv() {
        Path cwd = Paths.get("").toAbsolutePath();
        Path backendEnv = cwd.resolve(".env");
        Path nestedBackendEnv = cwd.resolve("GOGAMING-backend").resolve(".env");

        if (Files.exists(backendEnv)) {
            logger.info("Using .env from {}", backendEnv);
            return Dotenv.configure()
                    .directory(cwd.toString())
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();
        }

        if (Files.exists(nestedBackendEnv)) {
            logger.info("Using .env from {}", nestedBackendEnv);
            return Dotenv.configure()
                    .directory(cwd.resolve("GOGAMING-backend").toString())
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();
        }

        logger.warn("No backend .env found in {} or {}. Falling back to default Dotenv lookup.", backendEnv, nestedBackendEnv);
        return Dotenv.configure()
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
    }
}

