/**
 * Cliente de la API de GoGaming y la lógica de las herramientas MCP.
 * No depende del SDK de MCP: así se puede probar con un fetch simulado.
 */

export const CATEGORIAS = ['raton', 'teclado', 'skate', 'alfombrilla', 'auriculares', 'manga'] as const;
export type Categoria = (typeof CATEGORIAS)[number];

const TIPO_A_CATEGORIA: Record<string, Categoria> = {
  Raton: 'raton', Teclado: 'teclado', Skate: 'skate', Alfombrilla: 'alfombrilla', Auriculares: 'auriculares', Manga: 'manga',
};

/** Campos comunes; el resto de un producto son las especificaciones de su categoría. */
const CAMPOS_COMUNES = new Set(['id', 'nombre', 'marca', 'fechaPublicacion', 'precio', 'stock', 'tipoProducto', 'image']);

export interface Producto {
  id: number;
  nombre: string;
  marca: string;
  precio: number;
  stock: number;
  tipoProducto: string;
  [campo: string]: unknown;
}

interface Pagina<T> {
  content: T[];
  totalPages: number;
}

interface Resumen {
  productId: number;
  averageRating: number;
  totalReviews: number;
}

export interface Pedido {
  id: number;
  userId: number;
  numeroTicket: string;
  fecha: string;
  total: number;
  estado: string;
  lineas: { productId: number; productName: string; cantidad: number; subtotal: number }[];
}

export interface Config {
  /** Por ejemplo http://localhost:8081/api/v1 */
  apiUrl: string;
  /** URL de la tienda, para enlazar las fichas. */
  webUrl: string;
  /** JWT de un administrador (solo para resumen_pedidos). */
  adminToken?: string;
  fetch?: typeof fetch;
}

export class ApiError extends Error {
  constructor(readonly status: number, message: string) {
    super(message);
  }
}

const euros = (n: number) => `${n.toLocaleString('es-ES', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} €`;

export class GoGaming {
  private readonly fetch: typeof fetch;

  constructor(private readonly config: Config) {
    this.fetch = config.fetch ?? fetch;
  }

  private async get<T>(ruta: string, token?: string): Promise<T> {
    const res = await this.fetch(`${this.config.apiUrl}${ruta}`, {
      headers: { Accept: 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
      signal: AbortSignal.timeout(15_000),
    });
    if (!res.ok) {
      throw new ApiError(res.status, `La API respondió ${res.status} en ${ruta}`);
    }
    return (await res.json()) as T;
  }

  private ficha(p: Producto): string {
    return `${this.config.webUrl}/productos/${TIPO_A_CATEGORIA[p.tipoProducto] ?? 'raton'}/${p.id}`;
  }

  private linea(p: Producto): string {
    const stock = p.stock <= 0 ? 'AGOTADO' : `${p.stock} uds.`;
    return `- [${p.id}] ${p.marca} ${p.nombre} (${p.tipoProducto}) · ${euros(p.precio)} · ${stock}`;
  }

  /** Todos los productos de una categoría (recorre las páginas). */
  private async todos(categoria: Categoria, nombre?: string): Promise<Producto[]> {
    const productos: Producto[] = [];
    for (let page = 0; ; page++) {
      const q = new URLSearchParams({ page: String(page), size: '100', sort: 'nombre,asc' });
      if (nombre) q.set('nombre', nombre);
      const pagina = await this.get<Pagina<Producto>>(`/productos/${categoria}?${q}`);
      productos.push(...pagina.content);
      if (page + 1 >= pagina.totalPages) return productos;
    }
  }

  async buscarProductos(texto: string, categoria?: Categoria, max = 20): Promise<string> {
    const categorias = categoria ? [categoria] : [...CATEGORIAS];
    const encontrados = (await Promise.all(categorias.map(c => this.todos(c, texto.trim())))).flat();
    if (encontrados.length === 0) {
      return `No hay productos que coincidan con «${texto}»${categoria ? ` en ${categoria}` : ''}.`;
    }
    const mostrados = encontrados.slice(0, max);
    const extra = encontrados.length > max ? `\n(${encontrados.length - max} más; afina la búsqueda o sube «max»)` : '';
    return `${encontrados.length} producto(s) para «${texto}»:\n${mostrados.map(p => this.linea(p)).join('\n')}${extra}`;
  }

  async verProducto(id: number): Promise<string> {
    let p: Producto;
    try {
      p = await this.get<Producto>(`/productos/${id}`);
    } catch (e) {
      if (e instanceof ApiError && e.status === 404) return `No existe ningún producto con id ${id}.`;
      throw e;
    }
    const resumen = await this.get<Record<string, Resumen>>(`/productos/resenas/resumen?ids=${id}`);
    const r = resumen[String(id)];
    const specs = Object.entries(p)
      .filter(([k, v]) => !CAMPOS_COMUNES.has(k) && v !== null && v !== '')
      .map(([k, v]) => `  - ${k}: ${v}`);
    return [
      `${p.marca} ${p.nombre} (${p.tipoProducto}, id ${p.id})`,
      `Precio: ${euros(p.precio)} · Stock: ${p.stock <= 0 ? 'agotado' : `${p.stock} uds.`}`,
      `Valoración: ${r && r.totalReviews > 0 ? `${r.averageRating}/5 (${r.totalReviews} reseñas)` : 'sin reseñas'}`,
      'Especificaciones:',
      ...(specs.length ? specs : ['  (ninguna)']),
      `Ficha: ${this.ficha(p)}`,
    ].join('\n');
  }

  async consultarStock(umbral = 5, categoria?: Categoria): Promise<string> {
    const categorias = categoria ? [categoria] : [...CATEGORIAS];
    const productos = (await Promise.all(categorias.map(c => this.todos(c)))).flat();
    const bajos = productos.filter(p => p.stock <= umbral).sort((a, b) => a.stock - b.stock);
    const agotados = bajos.filter(p => p.stock <= 0).length;
    const cabecera = `${productos.length} productos revisados${categoria ? ` en ${categoria}` : ''}: ${agotados} agotado(s) y ${bajos.length - agotados} con ${umbral} uds. o menos.`;
    return bajos.length ? `${cabecera}\n${bajos.map(p => this.linea(p)).join('\n')}` : `${cabecera}\nNo hay que reponer nada.`;
  }

  async resumenPedidos(dias?: number): Promise<string> {
    if (!this.config.adminToken) {
      return 'Para consultar pedidos hace falta un JWT de administrador en la variable GOGAMING_ADMIN_TOKEN (consulta el README del servidor MCP).';
    }
    let pedidos: Pedido[];
    try {
      pedidos = await this.get<Pedido[]>('/payments/tickets/all', this.config.adminToken);
    } catch (e) {
      if (e instanceof ApiError && (e.status === 401 || e.status === 403)) {
        return 'La API rechaza el token (caducado o sin rol ADMIN). Los JWT duran 1 hora: inicia sesión como admin y actualiza GOGAMING_ADMIN_TOKEN.';
      }
      throw e;
    }
    if (dias) {
      const desde = Date.now() - dias * 86_400_000;
      pedidos = pedidos.filter(p => new Date(p.fecha).getTime() >= desde);
    }
    const periodo = dias ? `en los últimos ${dias} días` : 'en total';
    if (pedidos.length === 0) return `No hay pedidos ${periodo}.`;

    const facturado = pedidos.reduce((s, p) => s + Number(p.total), 0);
    const porEstado = new Map<string, number>();
    const porProducto = new Map<string, { uds: number; importe: number }>();
    for (const p of pedidos) {
      porEstado.set(p.estado, (porEstado.get(p.estado) ?? 0) + 1);
      for (const l of p.lineas) {
        const actual = porProducto.get(l.productName) ?? { uds: 0, importe: 0 };
        porProducto.set(l.productName, { uds: actual.uds + l.cantidad, importe: actual.importe + Number(l.subtotal) });
      }
    }
    const top = [...porProducto.entries()].sort((a, b) => b[1].uds - a[1].uds).slice(0, 5);
    const recientes = [...pedidos].sort((a, b) => b.fecha.localeCompare(a.fecha)).slice(0, 5);
    return [
      `${pedidos.length} pedido(s) ${periodo} · facturado ${euros(facturado)} · ticket medio ${euros(facturado / pedidos.length)}`,
      `Por estado: ${[...porEstado.entries()].map(([e, n]) => `${e} ${n}`).join(', ')}`,
      'Más vendidos:',
      ...top.map(([nombre, v]) => `  - ${nombre}: ${v.uds} uds. (${euros(v.importe)})`),
      'Últimos pedidos:',
      ...recientes.map(p => `  - ${p.numeroTicket} · ${p.fecha.slice(0, 16).replace('T', ' ')} · ${euros(Number(p.total))} · ${p.estado}`),
    ].join('\n');
  }
}
