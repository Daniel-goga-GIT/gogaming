import assert from 'node:assert/strict';
import { test } from 'node:test';
import { GoGaming, Pedido, Producto } from './gogaming.js';

const API = 'http://api.test/api/v1';

const raton: Producto = { id: 1, nombre: 'Beast X Pro', marca: 'WLMouse', precio: 139.99, stock: 0, tipoProducto: 'Raton', peso: 39, sensor: 'PAW3950', image: 'x' };
const teclado: Producto = { id: 39, nombre: 'Zenblade 65', marca: 'Pwnage', precio: 149.99, stock: 3, tipoProducto: 'Teclado', layout: '65%', retroiluminacion: null };
const skate: Producto = { id: 50, nombre: 'Jade', marca: 'X-raypad', precio: 14.99, stock: 20, tipoProducto: 'Skate' };

/** fetch simulado: responde según la ruta y guarda las peticiones. */
function fakeFetch(rutas: Record<string, unknown>, llamadas: { url: string; auth?: string }[] = []) {
  return (async (url: string | URL, init?: RequestInit) => {
    const u = String(url);
    const headers = (init?.headers ?? {}) as Record<string, string>;
    llamadas.push({ url: u, auth: headers.Authorization });
    const ruta = Object.keys(rutas).find(r => u.startsWith(API + r));
    if (!ruta) return new Response('', { status: 404 });
    const cuerpo = rutas[ruta];
    if (typeof cuerpo === 'number') return new Response('', { status: cuerpo });
    return Response.json(cuerpo);
  }) as typeof fetch;
}

const pagina = (content: Producto[]) => ({ content, totalPages: 1 });
const catalogo = {
  '/productos/raton': pagina([raton]), '/productos/teclado': pagina([teclado]), '/productos/skate': pagina([skate]),
  '/productos/alfombrilla': pagina([]), '/productos/auriculares': pagina([]), '/productos/manga': pagina([]),
};

test('buscar_productos busca en todas las categorías y marca los agotados', async () => {
  const llamadas: { url: string }[] = [];
  const api = new GoGaming({ apiUrl: API, webUrl: 'http://web', fetch: fakeFetch(catalogo, llamadas) });

  const texto = await api.buscarProductos('pro');

  assert.equal(llamadas.length, 6);
  assert.ok(llamadas.every(l => l.url.includes('nombre=pro')));
  assert.match(texto, /3 producto\(s\)/);
  assert.match(texto, /\[1\] WLMouse Beast X Pro \(Raton\) · 139,99 € · AGOTADO/);
});

test('buscar_productos en una sola categoría y sin resultados', async () => {
  const api = new GoGaming({ apiUrl: API, webUrl: 'http://web', fetch: fakeFetch({ '/productos/manga': pagina([]) }) });
  assert.equal(await api.buscarProductos('nada', 'manga'), 'No hay productos que coincidan con «nada» en manga.');
});

test('ver_producto muestra especificaciones, valoración y enlace a la ficha', async () => {
  const api = new GoGaming({
    apiUrl: API, webUrl: 'http://web',
    fetch: fakeFetch({ '/productos/39': teclado, '/productos/resenas/resumen': { 39: { productId: 39, averageRating: 4.5, totalReviews: 2 } } }),
  });

  const texto = await api.verProducto(39);

  assert.match(texto, /Pwnage Zenblade 65 \(Teclado, id 39\)/);
  assert.match(texto, /Valoración: 4.5\/5 \(2 reseñas\)/);
  assert.match(texto, /- layout: 65%/);
  assert.doesNotMatch(texto, /retroiluminacion/, 'los datos vacíos no se muestran');
  assert.match(texto, /Ficha: http:\/\/web\/productos\/teclado\/39/);
});

test('ver_producto con un id que no existe', async () => {
  const api = new GoGaming({ apiUrl: API, webUrl: 'http://web', fetch: fakeFetch({}) });
  assert.equal(await api.verProducto(999), 'No existe ningún producto con id 999.');
});

test('consultar_stock lista agotados y stock bajo, de menos a más', async () => {
  const api = new GoGaming({ apiUrl: API, webUrl: 'http://web', fetch: fakeFetch(catalogo) });

  const texto = await api.consultarStock(5);

  assert.match(texto, /3 productos revisados: 1 agotado\(s\) y 1 con 5 uds\. o menos/);
  assert.ok(texto.indexOf('Beast X Pro') < texto.indexOf('Zenblade'));
  assert.doesNotMatch(texto, /Jade/);
});

test('resumen_pedidos envía el token y resume ventas, estados y más vendidos', async () => {
  const hoy = new Date().toISOString();
  const pedidos: Pedido[] = [
    { id: 1, userId: 7, numeroTicket: 'GG-1', fecha: hoy, total: 279.98, estado: 'Enviado', lineas: [{ productId: 1, productName: 'Beast X Pro', cantidad: 2, subtotal: 279.98 }] },
    { id: 2, userId: 8, numeroTicket: 'GG-2', fecha: '2020-01-01T10:00:00', total: 14.99, estado: 'Preparando', lineas: [{ productId: 50, productName: 'Jade', cantidad: 1, subtotal: 14.99 }] },
  ];
  const llamadas: { url: string; auth?: string }[] = [];
  const api = new GoGaming({ apiUrl: API, webUrl: 'http://web', adminToken: 'jwt-admin', fetch: fakeFetch({ '/payments/tickets/all': pedidos }, llamadas) });

  const todo = await api.resumenPedidos();
  assert.equal(llamadas[0].auth, 'Bearer jwt-admin');
  assert.match(todo, /2 pedido\(s\) en total · facturado 294,97 €/);
  assert.match(todo, /Por estado: Enviado 1, Preparando 1/);
  assert.match(todo, /Beast X Pro: 2 uds\./);

  const ultimos = await api.resumenPedidos(30);
  assert.match(ultimos, /1 pedido\(s\) en los últimos 30 días/);
});

test('resumen_pedidos explica qué hacer sin token o con uno caducado', async () => {
  const sinToken = new GoGaming({ apiUrl: API, webUrl: 'http://web', fetch: fakeFetch({}) });
  assert.match(await sinToken.resumenPedidos(), /GOGAMING_ADMIN_TOKEN/);

  const caducado = new GoGaming({ apiUrl: API, webUrl: 'http://web', adminToken: 'viejo', fetch: fakeFetch({ '/payments/tickets/all': 401 }) });
  assert.match(await caducado.resumenPedidos(), /caducado o sin rol ADMIN/);
});

test('los errores de red o de servidor se propagan para que la herramienta los marque como error', async () => {
  const api = new GoGaming({ apiUrl: API, webUrl: 'http://web', fetch: fakeFetch({ '/productos/raton': 500 }) });
  await assert.rejects(api.buscarProductos('x', 'raton'), /500/);
});
