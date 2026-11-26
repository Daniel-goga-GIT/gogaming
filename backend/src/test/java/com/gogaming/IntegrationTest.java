package com.gogaming;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Base de los tests de integración: la aplicación completa contra una MariaDB real en Docker
 * (Testcontainers), con schema.sql y data.sql. Si no hay Docker, los tests se saltan.
 * Los valores del .env local no se usan: las propiedades de aquí tienen prioridad.
 */
@SpringBootTest(properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=2525",
        "spring.mail.username=test",
        "spring.mail.password=test",
        "spring.security.oauth2.client.registration.google.client-id=test",
        "spring.security.oauth2.client.registration.google.client-secret=test",
        "STRIPE_SECRET_KEY=sk_test_dummy",
        "STRIPE_WEBHOOK_SECRET=whsec_dummy",
        "OPENROUTER_API_KEY=",
        "FRONTEND_BASE_URL=http://localhost:4200",
        "cors.allowed-origins=http://localhost:4200",
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
public abstract class IntegrationTest {

    @Container
    @ServiceConnection
    static final MariaDBContainer MARIADB = new MariaDBContainer("mariadb:11.4");

    private static final String KEYSTORE_PASSWORD = "test-password";

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) throws Exception {
        Path dir = Files.createTempDirectory("gogaming-test");
        registry.add("jwt.keystore.path", () -> keystore(dir).toString());
        registry.add("jwt.keystore.password", () -> KEYSTORE_PASSWORD);
        registry.add("jwt.keystore.alias", () -> "jwt-keypair");
        registry.add("UPLOAD_PATH", () -> dir.resolve("uploads").toString());
    }

    /** Keystore JKS de usar y tirar, generado con el keytool del JDK. */
    private static Path keystore(Path dir) {
        Path jks = dir.resolve("jwt.jks");
        if (Files.exists(jks)) return jks;
        String keytool = Paths.get(System.getProperty("java.home"), "bin", "keytool").toString();
        try {
            Process p = new ProcessBuilder(keytool, "-genkeypair", "-alias", "jwt-keypair", "-keyalg", "RSA", "-keysize", "2048",
                    "-storetype", "JKS", "-keystore", jks.toString(), "-storepass", KEYSTORE_PASSWORD, "-keypass", KEYSTORE_PASSWORD,
                    "-dname", "CN=gogaming-test", "-validity", "1")
                    .redirectErrorStream(true).start();
            if (p.waitFor() != 0) {
                throw new IllegalStateException("keytool falló: " + new String(p.getInputStream().readAllBytes()));
            }
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return jks;
    }
}
