package com.gogaming.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Documentación de la API (Swagger UI en /swagger-ui-custom.html).
 * El botón «Authorize» acepta el JWT que devuelve /api/v1/twofactor.
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "GoGaming API",
        version = "1.0",
        description = "Catálogo de periféricos gaming, pagos con Stripe Checkout, reseñas y asistente con IA."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
