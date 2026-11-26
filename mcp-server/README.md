# Servidor MCP de GoGaming

Expone la API de la tienda como herramientas [MCP](https://modelcontextprotocol.io) para consultar el catálogo y las ventas desde Claude Code, Claude Desktop u otro cliente MCP.
Todas las herramientas son de **solo lectura**.

| Herramienta | Qué hace | Ejemplo de pregunta |
|---|---|---|
| `buscar_productos` | Busca por nombre o marca, en todas las categorías o en una | «¿Qué ratones de Pulsar tenemos?» |
| `ver_producto` | Precio, stock, valoración, especificaciones y enlace a la ficha | «Dame los detalles del producto 39» |
| `consultar_stock` | Productos agotados o con pocas unidades | «¿Qué tengo que reponer?» |
| `resumen_pedidos` | Pedidos, facturación, ticket medio, estados y más vendidos (admin) | «¿Cuánto hemos vendido este mes?» |

## Uso

```bash
cd mcp-server
npm install
npm run build
```

**Claude Code:** el repositorio ya incluye `.mcp.json` en la raíz. Al abrir Claude Code en el proyecto, aprueba el servidor `gogaming` (o compruébalo con `/mcp`).

**Claude Desktop** (`claude_desktop_config.json`):

```json
{
  "mcpServers": {
    "gogaming": {
      "command": "node",
      "args": ["/ruta/a/gogaming/mcp-server/dist/index.js"],
      "env": { "GOGAMING_API_URL": "http://localhost:8081/api/v1" }
    }
  }
}
```

### Variables de entorno

| Variable | Por defecto | Uso |
|---|---|---|
| `GOGAMING_API_URL` | `http://localhost:8081/api/v1` | API del backend |
| `GOGAMING_WEB_URL` | `http://localhost:4200` | Tienda, para enlazar las fichas |
| `GOGAMING_ADMIN_TOKEN` | — | JWT de un administrador; solo lo necesita `resumen_pedidos` |

El token de administrador es el que guarda la web en `localStorage` (`gogaming_token`) tras iniciar sesión como admin. Caduca en 1 hora: si la herramienta dice que lo rechaza, vuelve a iniciar sesión y actualiza la variable.

## Tests

```bash
npm test         # pruebas de la lógica con un fetch simulado
npm run smoke    # cliente MCP real por stdio contra el backend en marcha
```
