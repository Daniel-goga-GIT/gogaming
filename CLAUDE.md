# CLAUDE.md — GoGaming

Tienda online de periféricos gaming (ratones, teclados, alfombrillas, skates, auriculares/IEM) y mangas.
Es el proyecto principal del portfolio. Objetivo: dejarla con calidad profesional antes del **4 dic 2026**.
Plan general y calendario: `C:\Users\danie\Documents\Portfolio\CLAUDE.md`. Responde siempre en **español**.

## Estructura

- `backend/`: Spring Boot 4.0.1, Java 21, Maven (`mvnw`), MariaDB (docker-compose), JPA.
  Seguridad con JWT (keystore), 2FA por email, OAuth2 con Google, pagos con Stripe Checkout, reseñas y Swagger (springdoc).
  Paquete base: `com.gogaming` (clase principal `GogamingApplication`; artifact `gogaming-api`). Swagger: `/swagger-ui-custom.html` (JSON en `/api-docs`).
  Esquema y datos: `src/main/resources/schema.sql`, `data.sql`. Configuración por `.env` (dotenv-java).
- `frontend/`: Angular 20 (standalone, SSR), Tailwind 3.4, ngx-translate (es/en), Karma/Jasmine.
  Catálogo genérico: `core/catalogo.ts` (las 6 categorías: ruta, título, campos, icono) + `ProductoService`.
  `features/productos/listado` y `features/productos/detalle` sirven para todas las categorías (ver «Catálogo genérico»).
  `layout/ai-chat`: chat con IA (OpenRouter).
  API: `src/environments/environments.ts`.

## Modelo

`Producto` (tabla `productos`, herencia JOINED) → `Raton`, `Teclado`, `Alfombrilla`, `Skate`, `Auriculares`, `Manga`.
Las compras se guardan en `Ticket` y `LineaTicket`. También hay `Resena`, `User` y `Role`.

## Problemas detectados (23 nov 2026)

1. ~~API key de OpenRouter en el frontend~~ → revocada; ahora el chat pasa por el backend (`POST /api/v1/chat`).
2. `.env` y `.env.produc` tienen secretos reales (Stripe, Google, SMTP, JWT). Ya se han añadido al `.gitignore`. Los repositorios originales son **públicos**, así que hay que cambiar todas las claves. Borrarlas del repositorio no basta, porque siguen en el historial.
3. ~~El catálogo exigía login~~ → ya es público; el login se pide solo al pagar, al escribir reseñas y en el perfil.
4. ~~Código repetido por categoría~~ → backend y frontend unificados el 26 nov (ver «Catálogo genérico»).
5. ~~`pom.xml` con dependencias duplicadas y sin usar~~ → limpiado el 26 nov. También se ha renombrado el paquete y springdoc pasa a 3.0.3 (la rama de Spring Boot 4).
6. ~~Tailwind 3 y 4 mezclados~~ → solo Tailwind 3.4 (Angular lo detecta por `tailwind.config.js`; `postcss.config.js` no se usaba). Pasar a la v4 cambiaría valores por defecto y obligaría a revisar toda la UI.
7. ~~Stripe no estaba integrado~~ → **Stripe Checkout integrado** (ver sección «Pagos con Stripe»).
8. ~~Tests de relleno~~ → backend: 34 unitarios + 9 de integración con Testcontainers (`./mvnw verify`, necesita Docker); frontend: 31 con Karma (`npm run test:ci`) y `npm run i18n:check`. CI en `.github/workflows/ci.yml`. No hay README. El bundle inicial (634 kB) supera el presupuesto de 500 kB de `angular.json`. Repo git nuevo en `gogaming/` (historial limpio desde el 23 nov 2026).
9. ~~Login/registro/2FA y OAuth sin revisar~~ → revisados el 26 nov (ver «Autenticación»). Para que funcione Google falta **añadir `http://localhost:8081/oauth2/callback` como URI de redirección autorizada** en Google Cloud Console (hoy Google responde `redirect_uri_mismatch`).
10. ~~`permitAll` de productos sin filtrar por método~~ → ahora solo `GET /api/v1/productos/**` es público.

## Estado de las claves (23 nov 2026)

`.env` local actualizado con claves nuevas: OpenRouter (`OPENROUTER_API_KEY`, todavía no se usa en el código), Stripe (cuenta Sandbox nueva), Google OAuth (secret nuevo) y Gmail SMTP (`MAIL_USERNAME` + contraseña de aplicación).
`JWT_SECRET` no se usa: los JWT se firman con RS256 con el keystore. Pendiente: generar un keystore nuevo (el antiguo pudo filtrarse). `.env.produc` sigue con valores antiguos y filtrados: rehacerlo al desplegar.
Recomendado: volver a cambiar las claves que pasaron por el chat (OpenRouter y Google).

## Convenciones

- Nunca escribir secretos en el código; solo en `.env` (backend) o como variables de entorno al desplegar.
- Commits pequeños y en español.
- Hacer cambios por fases y comprobar después de cada una: `./mvnw verify` (backend, con Docker abierto), `npm run build`, `npm run test:ci` y `npm run i18n:check` (frontend).
- Tests del frontend: proveedores comunes en `src/testing/test-providers.ts` (HttpClient de pruebas con el interceptor real, router y traducciones que devuelven la clave).

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
- `core/auth.interceptor.ts` añade `Authorization` a las peticiones a nuestra API, y solo si hay token (`Bearer null` rompe los endpoints públicos). Los servicios nuevos no tienen que añadir cabeceras.
- Roles: `AuthService.isAdmin()` / `hasRole()` (el JWT puede tener varios roles).
- Revisión visual: script de Playwright con el Chrome del sistema (capturas en escritorio 1366 y móvil 390; usuario QA admin `danielgongar8+qa@gmail.com`, código 2FA leído de la tabla `usuarios.code`).
  También hay un usuario de pruebas sin permisos de admin, `danielgongar8+e2e@gmail.com` (id 68). Se usó para el pago E2E. En Stripe Checkout, el formulario de tarjeta está dentro del iframe `elements-inner-habanero`.

## Catálogo genérico

Backend:

- Un solo `ProductoController` (`/api/v1/productos`) + `ProductoService` + `ProductoRepository` para las 6 categorías. Las URLs y el JSON no han cambiado.
- `CategoriaProducto` (enum) une el segmento de la URL (`raton`, `teclado`, `skate`, `alfombrilla`, `auriculares`, `manga`) con `TipoProducto`, la entidad y el DTO. Añadir una categoría = entidad + DTO + una línea en el enum.
- Los DTO heredan de `ProductoDTO` y se validan con anotaciones (`@NotBlank`, `@NotNull`...); errores → 400 `{message, errors[]}`. Borrar un producto con pedidos o reseñas → 409.
- Entidad ↔ DTO con el `ObjectMapper` de Jackson 3 (mismos nombres de campo). `tipoProducto` lo fija el servidor según la URL.
- Solo se puede ordenar por campos comunes (`id`, `nombre`, `marca`, `precio`, `stock`, `fechaPublicacion`); se añade `id` como desempate.
- `GET /api/v1/productos/{id}` (numérico) devuelve un producto de cualquier categoría. `/home`: los 5 primeros ratones.

Frontend:
- `core/catalogo.ts`: equivalente del enum. Por categoría guarda el slug, `tipoProducto`, la ruta del listado (`/ratones`, `/iem-cascos`...), el título i18n, los campos (tipo, unidad, ejemplo, si son opcionales), los campos del resumen y el icono. Funciones: `categoriaPorSlug/Tipo`, `rutaProducto(p)` → `/productos/{slug}/{id}` y `urlImagen`.
- Rutas: un `ListadoProductos` por categoría (`data.categoria`) y `DetalleProducto` en `productos/:categoria/:id` (categoría o id desconocidos → «producto no encontrado»).
- La tabla de especificaciones de la ficha, y la tabla y el formulario del dashboard, se generan a partir de `campos`. Etiquetas: `SPECS.*`. Textos del dashboard: `ADMIN.*`.
- Añadir una categoría = backend (entidad + DTO + enum) + una entrada en `catalogo.ts` + textos i18n + enlace en el header.

## Autenticación

- Login en dos pasos: `POST /authenticate` (email + contraseña) → token **pendiente de 2FA** (claim `twofactor=true`, 5 min) + código de 6 cifras por email (`SecureRandom`). `POST /twofactor` con ese token y el código → token completo (1 h).
- `JwtAuthenticationFilter`: un token pendiente de 2FA **no autentica nunca**. Un token inválido o caducado se ignora (la petición sigue como anónima, así el catálogo funciona aunque el navegador guarde un token viejo).
- 2FA: 5 códigos erróneos invalidan el código; comparación en tiempo constante; el código no se puede reutilizar.
- Google: botón `shared/google-login-button` → `/oauth2/authorization/google` (backend) → Google → `/oauth2/callback` (backend) → `FRONTEND_BASE_URL/oauth2/callback#token=…` (frontend, `features/oauth-callback`). Fallos → `#error=…`. Solo se aceptan emails verificados por Google. Google hace de segundo factor.
- Registro validado también en el servidor (email, nombre, contraseña de 8+ caracteres con mayúscula y número). Fotos de perfil: solo JPG/PNG/WebP/GIF (por extensión y por los primeros bytes), servidas con `nosniff` y sin poder salir de `UPLOAD_PATH`.
- `/error` es público: si no, Spring Security convertía cualquier 400/500 en 401.

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

## Asistente con IA (chat)

- `ChatService` + `POST /api/v1/chat` (público, máx. 10 mensajes/min por IP, 1000 caracteres por mensaje, se envían los últimos 10 mensajes como historial).
- OpenRouter con modelos **gratuitos** en `OPENROUTER_MODELS` (lista separada por comas, en orden de preferencia). Cambian a menudo: si el chat deja de responder, mira `https://openrouter.ai/api/v1/models` y actualiza el `.env` sin tocar código.
- Los modelos gratuitos "razonan" antes de responder: se piden `reasoning.effort=low` y `max_tokens=2000` (con 600 devolvían la respuesta vacía). Ante un 429 se reintenta una vez y después se pasa al siguiente modelo.
- El prompt incluye el **catálogo real** (marca, producto, categoría, precio y stock, cacheado 5 min) y prohíbe inventar especificaciones.
- El frontend formatea las respuestas de forma segura (escapa el HTML y solo aplica **negritas** y viñetas).

## Próximos pasos (por orden)

1. ~~Probar un pago completo con la tarjeta 4242~~ → hecho el 26 nov con Playwright: pedido creado, stock descontado, confirmación idempotente y email enviado.
2. ~~Refactor del catálogo~~ → backend y frontend genéricos (26 nov). El dashboard también es genérico, con textos i18n y errores de validación del backend en los avisos.
3. ~~Limpieza: `pom.xml`, Tailwind y paquete `com.gogaming`~~ → hecho el 26 nov.
4. ~~Revisar login/registro/2FA y OAuth~~ → hecho el 26 nov (había una forma de saltarse el 2FA). Límite de intentos en `/authenticate`: 10 fallos por IP y email y 30 por IP cada 15 min → 429 (`utils/RateLimiter`, también lo usa el chat). **Al desplegar detrás de un proxy** hay que activar `server.forward-headers-strategy` para que la IP sea la del cliente. Dashboard: tabla paginada (20 por página) con búsqueda; pedidos con detalle de líneas y cambio de estado (Preparando/Enviado/Entregado, validado en el backend). **Cancelar un pedido no está implementado** (requiere reembolso en Stripe y reponer stock).
   ~~Valoraciones una a una~~ → `GET /api/v1/productos/resenas/resumen?ids=1,2,3` (una consulta agrupada). Reseñas en `/api/v1/productos/{id}/resenas`. **Una reseña por usuario y producto** (409 + índice único `uq_resenas_usuario_producto`).
5. ~~Tests~~ → hecho el 26 nov (ver punto 8 de «Problemas»). Falta el README con capturas.
6. Servidor MCP (`mcp-server/`) sobre la API: buscar productos, consultar stock y resumen de pedidos.
7. Despliegue (frontend + backend + BD gestionada) y webhook de Stripe en producción.
