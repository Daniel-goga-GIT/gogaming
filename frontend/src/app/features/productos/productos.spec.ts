import { HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';
import { environment } from '../../../environments/environments';
import { testProviders } from '../../../testing/test-providers';
import { DetalleProducto } from './detalle/detalle-producto';
import { ListadoProductos } from './listado/listado-productos';

const api = `${environment.apiUrl}/v1/productos`;

const teclado = {
  id: 39, nombre: 'Zenblade 65', marca: 'Pwnage', precio: 149.99, stock: 3, tipoProducto: 'Teclado',
  image: 'https://img/z.png', fechaPublicacion: '2024-10-20',
  tipoSwitch: 'Hall Effect', layout: '65% ANSI', conectividad: 'USB-C', retroiluminacion: null,
};

describe('Catálogo genérico', () => {
  let backend: HttpTestingController;

  function configurar(route: Partial<ActivatedRoute>) {
    TestBed.configureTestingModule({
      providers: [...testProviders(), { provide: ActivatedRoute, useValue: route }],
    });
    backend = TestBed.inject(HttpTestingController);
  }

  afterEach(() => backend.verify());

  it('el listado pide su categoría y las valoraciones de toda la página en una sola petición', () => {
    configurar({ data: of({ categoria: 'teclado' }) });
    const fixture = TestBed.createComponent(ListadoProductos);
    fixture.detectChanges();

    backend.expectOne(r => r.url === `${api}/teclado`).flush({ content: [teclado], totalElements: 1, totalPages: 1, number: 0, size: 10 });
    backend.expectOne(r => r.url === `${api}/resenas/resumen` && r.params.get('ids') === '39')
      .flush({ 39: { productId: 39, averageRating: 4.5, totalReviews: 2 } });
    fixture.detectChanges();

    const tarjetas = fixture.nativeElement.querySelectorAll('app-product-card');
    expect(tarjetas.length).toBe(1);
    expect(tarjetas[0].textContent).toContain('Zenblade 65');
    expect(fixture.nativeElement.querySelector('h1').textContent).toContain('PRODUCTS.TECLADOS_TITLE');
  });

  it('la ficha genera la tabla de especificaciones desde la definición de la categoría', () => {
    configurar({ paramMap: of(convertToParamMap({ categoria: 'teclado', id: '39' })) });
    const fixture = TestBed.createComponent(DetalleProducto);
    fixture.detectChanges();

    backend.expectOne(`${api}/teclado/39`).flush(teclado);
    backend.expectOne(`${api}/39/resenas`).flush({ summary: { totalReviews: 0, averageRating: 0 }, reviews: [] });
    fixture.detectChanges();

    const filas = Array.from(fixture.nativeElement.querySelectorAll('dl > div') as NodeListOf<HTMLElement>).map(
      div => `${div.querySelector('dt')!.textContent!.trim()}=${div.querySelector('dd')!.textContent!.trim()}`,
    );
    expect(filas).toEqual([
      'SPECS.CATEGORY=CATEGORY.Teclado',
      'SPECS.SWITCH=Hall Effect',
      'SPECS.LAYOUT=65% ANSI',
      'SPECS.CONNECTIVITY=USB-C',
      'SPECS.BACKLIGHT=—', // dato opcional vacío
    ]);
  });

  it('una categoría desconocida muestra «no encontrado» sin llamar a la API', () => {
    configurar({ paramMap: of(convertToParamMap({ categoria: 'patinete', id: '1' })) });
    const fixture = TestBed.createComponent(DetalleProducto);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('PRODUCT.NOT_FOUND');
    backend.expectNone(() => true);
  });

  it('un 404 de la API también muestra «no encontrado»', () => {
    configurar({ paramMap: of(convertToParamMap({ categoria: 'raton', id: '999' })) });
    const fixture = TestBed.createComponent(DetalleProducto);
    fixture.detectChanges();

    backend.expectOne(`${api}/raton/999`).flush(null, { status: 404, statusText: 'Not Found' });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('PRODUCT.NOT_FOUND');
  });
});
