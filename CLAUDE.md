# CLAUDE.md — GoGaming

Tienda online de periféricos gaming (ratones, teclados, alfombrillas, skates, auriculares/IEM) y mangas.
Es el proyecto principal del portfolio. Objetivo: dejarla con calidad profesional antes del **4 dic 2026**.
Plan general y calendario: `C:\Users\danie\Documents\Portfolio\CLAUDE.md`. Responde siempre en **español**.

## Estructura

- `backend/`: Spring Boot 4.0.1, Java 21, Maven (`mvnw`), MariaDB (docker-compose), JPA.
  Seguridad con JWT (keystore), 2FA por email, OAuth2 con Google, reseñas y Swagger (springdoc).
  Paquete base: `org.iesalixar.daw2.alejandromontero.ticket_logger_api` (viene de la plantilla de clase; se renombrará).
  Esquema y datos: `src/main/resources/schema.sql`, `data.sql`. Configuración por `.env` (dotenv-java).
- `frontend/`: Angular 20 (standalone, SSR), Tailwind, ngx-translate (es/en), Karma/Jasmine.
  `src/app/core/services`: un servicio por categoría. `features/productos/<categoria>`: listado y detalle.
  `layout/ai-chat`: chat con IA (OpenRouter).
  API: `src/environments/environments.ts`.

## Modelo

`Producto` (tabla `productos`, herencia JOINED) → `Raton`, `Teclado`, `Alfombrilla`, `Skate`, `Auriculares`, `Manga`.
Las compras se guardan en `Ticket` y `LineaTicket`. También hay `Resena`, `User` y `Role`.

## Problemas detectados (23 nov 2026)

1. **SEGURIDAD:** hay una API key de OpenRouter escrita en el código de `ai-chat.component.ts`, que se envía al navegador. Ya está revocada y quitada del código. Pendiente: mover la llamada a la IA al backend.
2. `.env` y `.env.produc` tienen secretos reales (Stripe, Google, SMTP, JWT). Ya se han añadido al `.gitignore`. Los repositorios originales son **públicos**, así que hay que cambiar todas las claves. Borrarlas del repositorio no basta, porque siguen en el historial.
3. Todo el catálogo exige login (`authGuardLogin`). En una tienda, el catálogo debería ser público.
4. Hay mucho código repetido: un controller, servicio, repositorio, mapper, DTO, servicio Angular y componente por cada categoría.
5. `pom.xml`: dependencias duplicadas (mariadb, security), Thymeleaf sin usar, `data-jdbc` junto a JPA, `annotations:RELEASE`. El artifact se llama `ticket-logger-api`.
6. Frontend: `tailwindcss` ^3 mezclado con `@tailwindcss/postcss` ^4.
7. **Stripe no está integrado de verdad.** No hay SDK de Stripe en el `pom.xml`. `POST /api/v1/payments/checkout-session` crea el ticket sin cobrar, y las variables `STRIPE_*` no se usan. Pendiente: implementar Stripe Checkout en modo test con webhook. Hay una cuenta nueva de Stripe (Sandbox) solo para GoGaming.
8. Los tests son casi todos los generados por defecto. No hay README. Repo git nuevo en `gogaming/` (historial limpio desde el 23 nov 2026).

## Estado de las claves (23 nov 2026)

`.env` local actualizado con claves nuevas: OpenRouter (`OPENROUTER_API_KEY`, todavía no se usa en el código), Stripe (cuenta Sandbox nueva), Google OAuth (secret nuevo) y Gmail SMTP (`MAIL_USERNAME` + contraseña de aplicación).
Pendiente: generar un JWT secret y un keystore nuevos. `.env.produc` sigue con valores antiguos y filtrados: rehacerlo al desplegar.
Recomendado: volver a cambiar las claves que pasaron por el chat (OpenRouter y Google).

## Convenciones

- Nunca escribir secretos en el código; solo en `.env` (backend) o como variables de entorno al desplegar.
- Commits pequeños y en español.
- Hacer cambios por fases y comprobar que el proyecto compila después de cada una (`./mvnw test`, `npm run build`).
