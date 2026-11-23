# CLAUDE.md — GoGaming

Tienda online de periféricos gaming (ratones, teclados, alfombrillas, skates, auriculares/IEM) y mangas.
Es el proyecto principal del portfolio. Objetivo: dejarla con calidad profesional antes del **4 dic 2026**.
Plan general y calendario: `C:\Users\danie\Documents\Portfolio\CLAUDE.md`. Responde siempre en **español**.

## Estructura

- `backend/`: Spring Boot 4.0.1, Java 21, Maven (`mvnw`), MariaDB (docker-compose), JPA.
  Seguridad con JWT (keystore), 2FA por email, OAuth2 con Google, pagos con Stripe Checkout, reseñas y Swagger (springdoc).
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
3. ~~El catálogo exigía login~~ → ya es público; el login se pide solo al pagar, al escribir reseñas y en el perfil.
4. Hay mucho código repetido: un controller, servicio, repositorio, mapper, DTO, servicio Angular y componente por cada categoría.
5. `pom.xml`: dependencias duplicadas (mariadb, security), Thymeleaf sin usar, `data-jdbc` junto a JPA, `annotations:RELEASE`. El artifact se llama `ticket-logger-api`.
6. Frontend: `tailwindcss` ^3 mezclado con `@tailwindcss/postcss` ^4.
7. ~~Stripe no estaba integrado~~ → **Stripe Checkout integrado** (ver sección «Pagos con Stripe»).
8. Los tests son casi todos los generados por defecto. No hay README. Repo git nuevo en `gogaming/` (historial limpio desde el 23 nov 2026).

## Estado de las claves (23 nov 2026)

`.env` local actualizado con claves nuevas: OpenRouter (`OPENROUTER_API_KEY`, todavía no se usa en el código), Stripe (cuenta Sandbox nueva), Google OAuth (secret nuevo) y Gmail SMTP (`MAIL_USERNAME` + contraseña de aplicación).
Pendiente: generar un JWT secret y un keystore nuevos. `.env.produc` sigue con valores antiguos y filtrados: rehacerlo al desplegar.
Recomendado: volver a cambiar las claves que pasaron por el chat (OpenRouter y Google).

## Convenciones

- Nunca escribir secretos en el código; solo en `.env` (backend) o como variables de entorno al desplegar.
- Commits pequeños y en español.
- Hacer cambios por fases y comprobar que el proyecto compila después de cada una (`./mvnw test`, `npm run build`).

## Cómo arrancar en local

1. Abrir Docker Desktop y ejecutar `cd backend && docker compose up -d` (MariaDB 11.4 en 127.0.0.1:3306, BD `gogaming`).
2. Backend: `cd backend && ./mvnw spring-boot:run` → http://localhost:8081. Lee `backend/.env`; la plantilla de variables está en `.env.example`.
   El puerto 8080 está ocupado en este PC por un proceso Java de NetWitSec.exe (software de seguridad). No tocarlo; por eso `SERVER_PORT=8081`.
3. Frontend: `cd frontend && npm start` → http://localhost:4200 (API en `src/environments/environments.ts`).
- Keystore JWT: `backend/storage/certs/jwt.jks` (fuera de git; si se pierde, regenerarlo con keytool, alias `jwt-keypair`).
- Endpoints públicos de productos: `/api/v1/productos/home`, `/api/v1/productos/<categoria>[/{id}]`.

## Convenciones de UI (frontend)

- **Nunca `alert()`/`confirm()`**: usar `NotificationService` (toasts: success/error/warning/info, con imagen y acción opcionales) y `ConfirmDialogService.confirm({...})` (devuelve `Promise<boolean>`, `variant: 'danger'` para borrar). Los dos se montan una sola vez en `app.html`.
- Añadir a la cesta desde las fichas → `CartFeedbackService.add(producto)`, que respeta el stock y muestra el aviso adecuado.
- Tarjetas de producto → `shared/product-card` (home, listados y buscador). Precios → pipe `price` (`1.299,99 €`).
- Todos los textos visibles van en `assets/i18n/es.json` y `en.json`, nunca escritos en la plantilla. Categorías: `CATEGORY.<tipoProducto>`.
- Enlaces internos con `routerLink`, nunca `href`, porque `href` recarga toda la app.
- Buscador global: `/buscar?q=` (busca por nombre o marca en las 6 categorías).
- Las peticiones solo llevan `Authorization` si hay token (`Bearer null` rompe los endpoints públicos).
- Roles: `AuthService.isAdmin()` / `hasRole()` (el JWT puede tener varios roles).
- Revisión visual: script de Playwright con el Chrome del sistema (capturas en escritorio 1366 y móvil 390; usuario QA admin `danielgongar8+qa@gmail.com`, código 2FA leído de la tabla `usuarios.code`).

## Pagos con Stripe

- `StripePaymentService` (stripe-java 34.2.0, `StripeClient`). Cuenta Stripe **Sandbox** solo para GoGaming (modo test).
- `POST /api/v1/payments/checkout-session` → valida la cesta contra la BD (precio, stock, máx. 10 uds./línea y 25 líneas) y devuelve `{sessionId, url}`. El precio cobrado va en la metadata `cart` = `id x cantidad @ céntimos;…`.
- El pedido (`Ticket`) se crea **solo cuando Stripe confirma el pago**, de dos maneras que conviven:
  - `GET /api/v1/payments/checkout-session/{id}`: el cliente vuelve a `/pago-completado?session_id=…`. Devuelve 200 con el pedido, 202 si sigue pendiente (el frontend reintenta 8 veces) o 404/403.
  - `POST /api/v1/payments/webhook`: público, validado con la firma `STRIPE_WEBHOOK_SECRET`. Atiende `checkout.session.completed` y `async_payment_succeeded`.
  - Es idempotente gracias a la columna única `tickets.stripe_session_id`: si llegan las dos confirmaciones a la vez, se crea un solo pedido.
- En local no hace falta el webhook (basta con la vuelta del cliente). Para probarlo: `stripe listen --forward-to localhost:8081/api/v1/payments/webhook` y copiar el `whsec_…` al `.env`.
- Al desplegar: crear el endpoint del webhook en el Dashboard de Stripe con la URL pública y poner su secreto en las variables de entorno.
- Tarjeta de prueba: `4242 4242 4242 4242`, cualquier fecha futura y CVC. Para probar el pago con 3D Secure: `4000 0027 6000 3184`.
- Número de pedido: `GG-AAMMDD-XXXXX` (aleatorio; el anterior por segundos chocaba con la restricción UNIQUE).
