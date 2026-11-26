# Despliegue

Lista de pasos para publicar GoGaming. La parte técnica está preparada: `backend/Dockerfile` y la configuración por variables de entorno.
Quedan las decisiones de la sección 1.

## 1. Decisiones pendientes

| Pieza | Opciones habituales | Notas |
|---|---|---|
| Frontend (estático) | Vercel, Netlify, Cloudflare Pages | Es una SPA: hay que redirigir todas las rutas a `index.html` |
| Backend (Docker) | Render, Railway, Fly.io, un VPS | Necesita un disco persistente para las fotos de perfil (`UPLOAD_PATH`) o se pierden al reiniciar |
| Base de datos | MariaDB/MySQL gestionada (Railway, Aiven…) | `schema.sql` y `data.sql` se ejecutan solos al arrancar |
| Dominio | — | Lo necesitan Google OAuth, Stripe y CORS |

## 2. Antes de publicar: claves nuevas

Los repositorios originales eran públicos y algunas claves pasaron por el chat (ver CLAUDE.md). Genera claves **nuevas** para producción:

- [ ] Stripe: claves de la cuenta (modo test o live) y el secreto del webhook de producción.
- [ ] Google OAuth: cliente o secreto nuevo.
- [ ] Gmail SMTP: contraseña de aplicación nueva.
- [ ] OpenRouter: API key nueva.
- [ ] Keystore JWT nuevo (`keytool …`, ver README). No reutilices el de desarrollo.
- [ ] Contraseña de la base de datos.

## 3. Backend

```bash
docker build -t gogaming-api ./backend
```

Variables (las de `backend/.env.example`), con especial atención a:

| Variable | Valor en producción |
|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Los de la BD gestionada |
| `FRONTEND_BASE_URL` | `https://tu-dominio` (de aquí salen CORS, la vuelta de Stripe y la de Google) |
| `BACKEND_BASE_URL` | URL pública del backend |
| `FORWARD_HEADERS_STRATEGY` | `framework` (ya viene en la imagen). Hace falta detrás del proxy de la plataforma para la IP real (límite de intentos) y la URL de OAuth |
| `JWT_KEYSTORE_PATH` / `JWT_KEYSTORE_PASSWORD` | Keystore montado como secreto o volumen (`/app/certs/jwt.jks` en la imagen) |
| `UPLOAD_PATH` | Disco persistente (`/app/uploads` en la imagen) |

Pendiente valorar: `spring.jpa.hibernate.ddl-auto=update` es cómodo, pero en producción lo profesional es un sistema de migraciones (Flyway).

## 4. Frontend

1. Crear `src/environments/environments.prod.ts` con `apiUrl: 'https://URL-DEL-BACKEND/api'`.
2. Añadir el `fileReplacements` correspondiente a la configuración `production` de `angular.json`.
3. `npm run build` → publicar `dist/gogaming/browser`, con la regla de reescritura de la SPA a `index.html`.

## 5. Servicios externos

- [ ] **Google Cloud Console** → credenciales OAuth → URI de redirección autorizada: `BACKEND_BASE_URL/oauth2/callback`. La de local (`http://localhost:8081/oauth2/callback`) ya está añadida.
- [ ] **Stripe Dashboard** → Webhooks → endpoint `BACKEND_BASE_URL/api/v1/payments/webhook` con los eventos `checkout.session.completed` y `checkout.session.async_payment_succeeded`. Copia su secreto en `STRIPE_WEBHOOK_SECRET`.
- [ ] **Administrador:** regístrate en la web y ejecuta en la BD
  `INSERT INTO user_roles (user_id, role_id) SELECT idusuario, 1 FROM usuarios WHERE correo_electronico = '…';`

## 6. Comprobación final

- [ ] Catálogo, buscador y fichas cargan; la consola del navegador no muestra errores.
- [ ] Registro, login con 2FA (llega el email) y login con Google.
- [ ] Pago con `4242 4242 4242 4242`: se crea el pedido, baja el stock y llega el email. En el Dashboard de Stripe, el webhook responde 200.
- [ ] El dashboard de admin funciona y un usuario normal no puede entrar.
- [ ] `https://…/swagger-ui-custom.html` responde. Si no se quiere público, protégelo o desactívalo con `springdoc.api-docs.enabled=false`.
