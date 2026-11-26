#!/usr/bin/env node
/**
 * Servidor MCP de GoGaming (stdio). Expone la API de la tienda como herramientas de solo lectura
 * para usarlas desde Claude Code, Claude Desktop u otro cliente MCP.
 *
 * Variables de entorno:
 *   GOGAMING_API_URL      API del backend (por defecto http://localhost:8081/api/v1)
 *   GOGAMING_WEB_URL      tienda, para enlazar fichas (por defecto http://localhost:4200)
 *   GOGAMING_ADMIN_TOKEN  JWT de un administrador; solo lo necesita resumen_pedidos
 */
import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import { z } from 'zod';
import { CATEGORIAS, GoGaming } from './gogaming.js';

const api = new GoGaming({
  apiUrl: (process.env.GOGAMING_API_URL ?? 'http://localhost:8081/api/v1').replace(/\/$/, ''),
  webUrl: (process.env.GOGAMING_WEB_URL ?? 'http://localhost:4200').replace(/\/$/, ''),
  adminToken: process.env.GOGAMING_ADMIN_TOKEN || undefined,
});

const server = new McpServer({ name: 'gogaming', version: '1.0.0' });
const soloLectura = { readOnlyHint: true, openWorldHint: false } as const;
const categoria = z.enum(CATEGORIAS).optional()
  .describe('Limita a una categoría. Sin ella se buscan las seis.');

/** Envuelve cada herramienta: texto como resultado y los fallos de red/API como error legible. */
async function responder(fn: () => Promise<string>) {
  try {
    return { content: [{ type: 'text' as const, text: await fn() }] };
  } catch (e) {
    const motivo = e instanceof Error ? e.message : String(e);
    return {
      isError: true,
      content: [{ type: 'text' as const, text: `No se ha podido consultar la API de GoGaming (${motivo}). ¿Está el backend arrancado?` }],
    };
  }
}

server.registerTool(
  'buscar_productos',
  {
    title: 'Buscar productos',
    description:
      'Busca productos del catálogo de GoGaming por nombre o marca (sin distinguir mayúsculas). ' +
      'Devuelve id, marca, nombre, categoría, precio y stock. Usa ver_producto con el id para ver las especificaciones.',
    inputSchema: {
      texto: z.string().min(1).describe('Parte del nombre o de la marca, p. ej. "pulsar" o "superlight"'),
      categoria,
      max: z.number().int().min(1).max(50).default(20).describe('Máximo de resultados'),
    },
    annotations: soloLectura,
  },
  async ({ texto, categoria: c, max }) => responder(() => api.buscarProductos(texto, c, max)),
);

server.registerTool(
  'ver_producto',
  {
    title: 'Ver un producto',
    description: 'Detalle de un producto por id: precio, stock, valoración media, especificaciones de su categoría y enlace a la ficha.',
    inputSchema: { id: z.number().int().positive().describe('id del producto (lo da buscar_productos)') },
    annotations: soloLectura,
  },
  async ({ id }) => responder(() => api.verProducto(id)),
);

server.registerTool(
  'consultar_stock',
  {
    title: 'Consultar stock bajo',
    description: 'Lista los productos agotados o con pocas unidades, ordenados de menos a más stock. Útil para decidir qué reponer.',
    inputSchema: {
      umbral: z.number().int().min(0).max(1000).default(5).describe('Se listan los productos con este stock o menos'),
      categoria,
    },
    annotations: soloLectura,
  },
  async ({ umbral, categoria: c }) => responder(() => api.consultarStock(umbral, c)),
);

server.registerTool(
  'resumen_pedidos',
  {
    title: 'Resumen de pedidos',
    description:
      'Resumen de ventas para administradores: número de pedidos, facturación, ticket medio, pedidos por estado, ' +
      'productos más vendidos y últimos pedidos. Requiere GOGAMING_ADMIN_TOKEN.',
    inputSchema: {
      dias: z.number().int().min(1).max(3650).optional().describe('Solo los pedidos de los últimos N días. Sin él, todos.'),
    },
    annotations: soloLectura,
  },
  async ({ dias }) => responder(() => api.resumenPedidos(dias)),
);

await server.connect(new StdioServerTransport());
// stdout es el canal del protocolo: los mensajes de diagnóstico van a stderr.
console.error('Servidor MCP de GoGaming listo (stdio).');
