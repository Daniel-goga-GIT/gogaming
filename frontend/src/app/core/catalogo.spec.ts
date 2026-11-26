import { CATEGORIAS, categoriaPorSlug, categoriaPorTipo, rutaProducto, urlImagen } from './catalogo';

describe('catalogo', () => {
  it('tiene las seis categorías con slug, tipo y ruta únicos', () => {
    expect(CATEGORIAS.length).toBe(6);
    for (const clave of ['slug', 'tipo', 'ruta'] as const) {
      expect(new Set(CATEGORIAS.map(c => c[clave])).size).withContext(clave).toBe(6);
    }
    CATEGORIAS.forEach(c => {
      expect(c.ruta.startsWith('/')).toBeTrue();
      expect(c.campos.length).toBeGreaterThan(0);
      c.resumen.forEach(r => expect(c.campos.map(f => f.clave)).withContext(c.slug).toContain(r));
    });
  });

  it('encuentra la categoría por slug y por tipoProducto', () => {
    expect(categoriaPorSlug('auriculares')?.ruta).toBe('/iem-cascos');
    expect(categoriaPorTipo('Manga')?.slug).toBe('manga');
    expect(categoriaPorSlug('patinete')).toBeUndefined();
  });

  it('construye la ruta de la ficha según el tipo del producto', () => {
    expect(rutaProducto({ id: 39, tipoProducto: 'Teclado' })).toEqual(['/productos', 'teclado', 39]);
  });

  it('limpia las comillas de las URLs de imagen y usa una por defecto', () => {
    expect(urlImagen('"https://img/x.png" ')).toBe('https://img/x.png');
    expect(urlImagen(null)).toBe('raton.png');
  });
});
