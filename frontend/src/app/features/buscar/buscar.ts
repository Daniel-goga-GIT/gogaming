import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { catchError, forkJoin, map, of, switchMap, tap } from 'rxjs';
import { environment } from '../../../environments/environments';
import { ProductoDTO } from '../../models/types';
import { ProductCard } from '../../shared/product-card/product-card';

/** Categorías del catálogo: endpoint de la API y ruta de la ficha en el frontend. */
const CATEGORIES = [
  { api: 'raton', route: '/productos/raton' },
  { api: 'teclado', route: '/productos/teclado' },
  { api: 'skate', route: '/productos/skate' },
  { api: 'alfombrilla', route: '/productos/alfombrilla' },
  { api: 'auriculares', route: '/productos/auriculares' },
  { api: 'manga', route: '/productos/manga' },
] as const;

interface SearchResult {
  product: ProductoDTO;
  route: string;
}

/**
 * Buscador global: busca por nombre o marca en todas las categorías a la vez.
 * Ruta: /buscar?q=texto
 */
@Component({
  selector: 'app-buscar',
  standalone: true,
  imports: [ProductCard, RouterLink, TranslateModule],
  template: `
    <section class="mx-auto w-[90%] md:w-[75%] py-10 min-h-[60vh]">
      <h1 class="text-2xl md:text-3xl font-bold text-gray-900">
        @if (query()) {
          {{ 'SEARCH.RESULTS_FOR' | translate }} <span class="text-[#CC0000]">“{{ query() }}”</span>
        } @else {
          {{ 'SEARCH.TITLE' | translate }}
        }
      </h1>

      @if (loading()) {
        <div class="mt-8 grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-3 sm:gap-6" aria-busy="true">
          @for (i of skeleton; track i) {
            <div class="h-72 rounded-xl border border-gray-100 bg-gray-50 animate-pulse"></div>
          }
        </div>
      } @else if (error()) {
        <p class="mt-8 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{{ 'SEARCH.ERROR' | translate }}</p>
      } @else if (query() && results().length === 0) {
        <div class="mt-10 rounded-2xl border border-dashed border-gray-200 bg-gray-50 px-6 py-14 text-center">
          <p class="text-lg font-semibold text-gray-900">{{ 'SEARCH.EMPTY_TITLE' | translate }}</p>
          <p class="mt-1 text-sm text-gray-600">{{ 'SEARCH.EMPTY_MSG' | translate }}</p>
          <a routerLink="/ratones" class="mt-6 inline-flex rounded-lg bg-[#CC0000] px-5 py-2.5 text-sm font-semibold text-white hover:bg-red-700">{{ 'PROFILE.VIEW_CATALOG' | translate }}</a>
        </div>
      } @else if (query()) {
        <p class="mt-2 text-sm text-gray-500">{{ 'SEARCH.COUNT' | translate: { count: results().length } }}</p>
        <div class="mt-6 grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-3 sm:gap-6">
          @for (r of results(); track r.route + r.product.id) {
            <app-product-card [product]="r.product" [link]="[r.route, r.product.id]" [image]="r.product.image" />
          }
        </div>
      }
    </section>
  `,
})
export class Buscar {
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);

  protected readonly query = signal('');
  protected readonly results = signal<SearchResult[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal(false);
  protected readonly skeleton = [1, 2, 3, 4, 5];

  constructor() {
    this.route.queryParamMap
      .pipe(
        map(p => (p.get('q') ?? '').trim()),
        tap(q => {
          this.query.set(q);
          this.error.set(false);
          this.loading.set(!!q);
          if (!q) this.results.set([]);
        }),
        switchMap(q => (q ? this.search(q) : of([]))),
        takeUntilDestroyed(inject(DestroyRef)),
      )
      .subscribe(results => {
        this.results.set(results);
        this.loading.set(false);
      });
  }

  private search(q: string) {
    const params = new HttpParams().set('nombre', q).set('size', '50').set('sort', 'nombre,asc');
    const requests = CATEGORIES.map(c =>
      this.http.get<{ content: ProductoDTO[] }>(`${environment.apiUrl}/v1/productos/${c.api}`, { params }).pipe(
        map(page => (page?.content ?? []).map(product => ({ product, route: c.route }))),
        catchError(() => {
          this.error.set(true);
          return of([] as SearchResult[]);
        }),
      ),
    );
    return forkJoin(requests).pipe(map(groups => groups.flat()));
  }
}
