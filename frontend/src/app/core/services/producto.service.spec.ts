import { HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environments';
import { testProviders } from '../../../testing/test-providers';
import { ProductoService } from './producto.service';
import { RatingService } from './rating.service';

describe('ProductoService y RatingService', () => {
  const api = `${environment.apiUrl}/v1/productos`;
  let productos: ProductoService;
  let valoraciones: RatingService;
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: testProviders() });
    productos = TestBed.inject(ProductoService);
    valoraciones = TestBed.inject(RatingService);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('lista una categoría con paginación, orden y filtro', () => {
    productos.listar('teclado', { page: 2, size: 5, sort: 'precio', direction: 'desc', nombre: '  wooting ' }).subscribe();

    const req = backend.expectOne(r => r.url === `${api}/teclado`);
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('size')).toBe('5');
    expect(req.request.params.get('sort')).toBe('precio,desc');
    expect(req.request.params.get('nombre')).toBe('wooting');
    req.flush({ content: [], totalElements: 0, totalPages: 0, number: 2, size: 5 });
  });

  it('no manda el parámetro nombre si el filtro está vacío', () => {
    productos.listar('raton').subscribe();
    const req = backend.expectOne(r => r.url === `${api}/raton`);
    expect(req.request.params.has('nombre')).toBeFalse();
    expect(req.request.params.get('sort')).toBe('id,asc');
    req.flush({ content: [] });
  });

  it('crea, edita y borra en la URL de la categoría', () => {
    productos.crear('skate', { nombre: 'X' }).subscribe();
    productos.actualizar('skate', 7, { nombre: 'Y' }).subscribe();
    productos.eliminar('skate', 7).subscribe();

    expect(backend.expectOne({ method: 'POST', url: `${api}/skate` }).request.body).toEqual({ nombre: 'X' });
    backend.expectOne({ method: 'PUT', url: `${api}/skate/7` });
    backend.expectOne({ method: 'DELETE', url: `${api}/skate/7` });
  });

  it('pide los resúmenes de valoraciones de una página en una sola petición', () => {
    valoraciones.getSummaries([1, 2, 3]).subscribe();
    const req = backend.expectOne(r => r.url === `${api}/resenas/resumen`);
    expect(req.request.params.get('ids')).toBe('1,2,3');
    req.flush({});
  });

  it('sin productos no hace ninguna petición de valoraciones', () => {
    let resultado: unknown;
    valoraciones.getSummaries([]).subscribe(r => (resultado = r));
    expect(resultado).toEqual({});
    backend.expectNone(() => true);
  });
});
