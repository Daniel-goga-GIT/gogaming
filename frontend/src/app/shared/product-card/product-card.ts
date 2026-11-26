import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ProductoDTO } from '../../models/types';
import { PricePipe } from '../pipes/price.pipe';

/**
 * Tarjeta de producto común a la home y a todos los listados por categoría.
 */
@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [RouterLink, TranslateModule, PricePipe],
  template: `
    <a
      [routerLink]="link()"
      class="group relative flex h-full flex-col rounded-xl border border-gray-100 bg-white p-3 sm:p-4 shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:border-gray-200 hover:shadow-lg focus:outline-none focus-visible:ring-2 focus-visible:ring-[#CC0000]"
      [attr.aria-label]="product().marca + ' ' + product().nombre"
    >
      <!-- Etiquetas -->
      <div class="absolute left-3 top-3 z-10 flex flex-col items-start gap-1 sm:left-4 sm:top-4">
        @if (soldOut()) {
          <span class="rounded bg-gray-700 px-1.5 py-0.5 text-[10px] font-bold uppercase tracking-wide text-white">{{ 'PRODUCT.SOLD_OUT' | translate }}</span>
        } @else if (featured()) {
          <span class="rounded bg-[#CC0000] px-1.5 py-0.5 text-[10px] font-bold uppercase tracking-wide text-white">{{ 'PRODUCT.FEATURED' | translate }}</span>
        }
        @if (lowStock()) {
          <span class="rounded bg-amber-100 px-1.5 py-0.5 text-[10px] font-bold text-amber-800">{{ 'PRODUCT.LOW_STOCK' | translate: { count: product().stock } }}</span>
        }
      </div>

      <!-- Imagen -->
      <div class="mb-3 flex h-36 items-center justify-center sm:h-48" [class.opacity-40]="soldOut()">
        <img
          [src]="image()"
          [alt]="product().nombre"
          loading="lazy"
          class="max-h-full max-w-full object-contain transition-transform duration-300 group-hover:scale-105"
        />
      </div>

      <!-- Información -->
      <div class="flex flex-1 flex-col">
        <p class="text-xs font-semibold uppercase tracking-wide text-gray-500">{{ product().marca }}</p>
        <h3 class="mt-0.5 line-clamp-2 min-h-[2.5rem] text-sm font-semibold leading-tight text-gray-900 sm:text-[15px]">{{ product().nombre }}</h3>

        <div class="mt-2 flex items-center gap-1.5 text-xs">
          @if (reviews() > 0) {
            <span class="flex items-center gap-0.5" aria-hidden="true">
              @for (star of stars; track star) {
                <svg class="h-3.5 w-3.5" [class]="rating() >= star - 0.25 ? 'text-amber-400' : 'text-gray-200'" fill="currentColor" viewBox="0 0 20 20"><path d="M9.05 2.93c.3-.92 1.6-.92 1.9 0l1.07 3.29a1 1 0 00.95.69h3.46c.97 0 1.37 1.24.59 1.81l-2.8 2.03a1 1 0 00-.36 1.12l1.07 3.29c.3.92-.76 1.69-1.54 1.12l-2.8-2.03a1 1 0 00-1.18 0l-2.8 2.03c-.78.57-1.84-.2-1.54-1.12l1.07-3.29a1 1 0 00-.36-1.12L2.98 8.72c-.78-.57-.38-1.81.59-1.81h3.46a1 1 0 00.95-.69l1.07-3.29z" /></svg>
              }
            </span>
            <span class="font-semibold text-gray-800">{{ rating().toFixed(1) }}</span>
            <span class="text-gray-500">({{ reviews() }})</span>
            <span class="sr-only">{{ (reviews() === 1 ? 'PRODUCT.REVIEWS_COUNT_ONE' : 'PRODUCT.REVIEWS_COUNT') | translate: { count: reviews() } }}</span>
          } @else {
            <span class="text-gray-400">{{ 'PRODUCT.NO_REVIEWS' | translate }}</span>
          }
        </div>

        <div class="mt-auto pt-3">
          <p class="text-lg font-bold text-gray-900 sm:text-xl">{{ product().precio | price }}</p>
          @if (!soldOut()) {
            <p class="mt-1 flex items-center gap-1 text-[11px] font-semibold text-green-700 sm:text-xs">
              <svg class="h-3.5 w-3.5 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="M3 7h11v8H3zM14 10h4l3 3v2h-7M7.5 18.5a1.5 1.5 0 100-3 1.5 1.5 0 000 3zM17.5 18.5a1.5 1.5 0 100-3 1.5 1.5 0 000 3z" /></svg>
              {{ 'PRODUCT.FREE_SHIPPING' | translate }}
            </p>
          }
        </div>
      </div>
    </a>
  `,
})
export class ProductCard {
  readonly product = input.required<ProductoDTO>();
  readonly link = input.required<unknown[] | string>();
  readonly image = input<string>('');
  readonly rating = input<number>(0);
  readonly reviews = input<number>(0);
  readonly featured = input<boolean>(false);

  protected readonly stars = [1, 2, 3, 4, 5];
  protected readonly soldOut = computed(() => (this.product().stock ?? 0) <= 0);
  protected readonly lowStock = computed(() => {
    const s = this.product().stock ?? 0;
    return s > 0 && s <= 5;
  });
}
