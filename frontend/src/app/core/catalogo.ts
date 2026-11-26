import { ProductoDTO } from '../models/types';

/**
 * Definición de las categorías del catálogo. Es el equivalente del enum CategoriaProducto del backend.
 * El listado, la ficha, el buscador y el dashboard se construyen a partir de aquí:
 * para añadir una categoría basta con añadir una entrada (y sus textos en i18n).
 */

export type SlugCategoria = 'raton' | 'teclado' | 'skate' | 'alfombrilla' | 'auriculares' | 'manga';

export interface CampoProducto {
  /** Nombre del campo en el DTO. */
  clave: string;
  /** Clave i18n de la etiqueta (SPECS.*). */
  etiqueta: string;
  tipo: 'texto' | 'numero' | 'opciones';
  opciones?: string[];
  /** Incremento del input numérico (por defecto 1). */
  paso?: number;
  /** Unidad que se muestra junto al valor (g, mm, cm). */
  unidad?: string;
  /** Valor de ejemplo para el formulario del dashboard. */
  ejemplo?: string;
  /** El backend no lo exige. */
  opcional?: boolean;
}

export interface Categoria {
  slug: SlugCategoria;
  /** Valor de tipoProducto en la API. */
  tipo: string;
  /** Ruta del listado en el frontend (la ficha es /productos/{slug}/{id}). */
  ruta: string;
  /** Clave i18n del título del listado. */
  titulo: string;
  /** Campos propios de la categoría, en el orden en que se muestran. */
  campos: CampoProducto[];
  /** Campos que resumen el producto en la tabla del dashboard. */
  resumen: string[];
  /** Trazado SVG (24x24, stroke) del icono del dashboard. */
  icono: string;
}

export const CATEGORIAS: readonly Categoria[] = [
  {
    slug: 'raton', tipo: 'Raton', ruta: '/ratones', titulo: 'PRODUCTS.RATONES_TITLE',
    campos: [
      { clave: 'sensor', etiqueta: 'SPECS.SENSOR', tipo: 'texto', ejemplo: 'PAW3950' },
      { clave: 'peso', etiqueta: 'SPECS.WEIGHT', tipo: 'numero', unidad: 'g', paso: 0.1 },
      { clave: 'tipoAgarre', etiqueta: 'SPECS.GRIP', tipo: 'opciones', opciones: ['Palm', 'Claw', 'Fingertip'] },
      { clave: 'switches', etiqueta: 'SPECS.SWITCHES', tipo: 'texto', ejemplo: 'Omron Optical' },
      { clave: 'medidas', etiqueta: 'SPECS.MEASURES', tipo: 'texto', ejemplo: '122x62x37mm' },
    ],
    resumen: ['sensor'],
    icono: 'M9 3H7a2 2 0 00-2 2v14a2 2 0 002 2h10a2 2 0 002-2V5a2 2 0 00-2-2h-2M9 3a2 2 0 002 2h2a2 2 0 002-2M9 3v2m6-2v2',
  },
  {
    slug: 'teclado', tipo: 'Teclado', ruta: '/teclados', titulo: 'PRODUCTS.TECLADOS_TITLE',
    campos: [
      { clave: 'tipoSwitch', etiqueta: 'SPECS.SWITCH', tipo: 'texto', ejemplo: 'Cherry MX Red' },
      { clave: 'layout', etiqueta: 'SPECS.LAYOUT', tipo: 'texto', ejemplo: 'TKL ANSI' },
      { clave: 'conectividad', etiqueta: 'SPECS.CONNECTIVITY', tipo: 'texto', ejemplo: 'USB-C, Bluetooth' },
      { clave: 'retroiluminacion', etiqueta: 'SPECS.BACKLIGHT', tipo: 'texto', ejemplo: 'RGB', opcional: true },
    ],
    resumen: ['layout'],
    icono: 'M9 17V7m0 10a2 2 0 01-2 2H5a2 2 0 01-2-2V7a2 2 0 012-2h2a2 2 0 012 2m0 10a2 2 0 002 2h2a2 2 0 002-2M9 7a2 2 0 012-2h2a2 2 0 012 2m0 10V7m0 10a2 2 0 002 2h2a2 2 0 002-2V7a2 2 0 00-2-2h-2a2 2 0 00-2 2',
  },
  {
    slug: 'skate', tipo: 'Skate', ruta: '/skates', titulo: 'PRODUCTS.SKATES_TITLE',
    campos: [
      { clave: 'material', etiqueta: 'SPECS.MATERIAL', tipo: 'texto', ejemplo: 'PTFE' },
      { clave: 'grosor', etiqueta: 'SPECS.THICKNESS', tipo: 'numero', unidad: 'mm', paso: 0.01, opcional: true },
      { clave: 'compatibilidad', etiqueta: 'SPECS.COMPATIBILITY', tipo: 'texto', ejemplo: 'Logitech G Pro X' },
    ],
    resumen: ['material'],
    icono: 'M4 8h16M4 16h16',
  },
  {
    slug: 'alfombrilla', tipo: 'Alfombrilla', ruta: '/alfombrillas', titulo: 'PRODUCTS.ALFOMBRILLAS_TITLE',
    campos: [
      { clave: 'dimensiones', etiqueta: 'SPECS.DIMENSIONS', tipo: 'texto', ejemplo: '490x420mm' },
      { clave: 'grosor', etiqueta: 'SPECS.THICKNESS', tipo: 'numero', unidad: 'mm', paso: 0.1, opcional: true },
      { clave: 'superficie', etiqueta: 'SPECS.SURFACE', tipo: 'texto', ejemplo: 'Cloth' },
    ],
    resumen: ['dimensiones'],
    icono: 'M4 6h16v12H4z',
  },
  {
    slug: 'auriculares', tipo: 'Auriculares', ruta: '/iem-cascos', titulo: 'PRODUCTS.AURICULARES_TITLE',
    campos: [
      { clave: 'tipo', etiqueta: 'SPECS.TYPE', tipo: 'texto', ejemplo: 'IEM' },
      { clave: 'driver', etiqueta: 'SPECS.DRIVER', tipo: 'texto', ejemplo: '10mm Dynamic' },
      { clave: 'conectividad', etiqueta: 'SPECS.CONNECTIVITY', tipo: 'texto', ejemplo: '3.5mm' },
      { clave: 'impedancia', etiqueta: 'SPECS.IMPEDANCE', tipo: 'texto', ejemplo: '32Ω', opcional: true },
    ],
    resumen: ['tipo'],
    icono: 'M9 19V6l12-3v13M9 19c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zm12-3c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zM9 10l12-3',
  },
  {
    slug: 'manga', tipo: 'Manga', ruta: '/mangas', titulo: 'PRODUCTS.MANGAS_TITLE',
    campos: [
      { clave: 'talla', etiqueta: 'SPECS.SIZE', tipo: 'texto', ejemplo: 'M' },
      { clave: 'material', etiqueta: 'SPECS.MATERIAL', tipo: 'texto', ejemplo: 'Lycra' },
      { clave: 'largo', etiqueta: 'SPECS.LENGTH', tipo: 'numero', unidad: 'cm', paso: 0.1, opcional: true },
      { clave: 'color', etiqueta: 'SPECS.COLOR', tipo: 'texto', ejemplo: 'Negro' },
    ],
    resumen: ['talla', 'material'],
    icono: 'M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01',
  },
];

export function categoriaPorSlug(slug: string | null | undefined): Categoria | undefined {
  return CATEGORIAS.find(c => c.slug === slug);
}

export function categoriaPorTipo(tipo: string | null | undefined): Categoria | undefined {
  return CATEGORIAS.find(c => c.tipo === tipo);
}

/** Ruta de la ficha de un producto: /productos/{slug}/{id}. */
export function rutaProducto(producto: Pick<ProductoDTO, 'id' | 'tipoProducto'>): (string | number)[] {
  return ['/productos', categoriaPorTipo(producto.tipoProducto)?.slug ?? 'raton', producto.id];
}

/** Las URLs de imagen de la BD a veces vienen con comillas. */
export function urlImagen(url: string | null | undefined): string {
  return url ? url.replace(/"/g, '').trim() : 'raton.png';
}
