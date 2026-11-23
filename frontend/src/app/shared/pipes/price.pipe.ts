import { Pipe, PipeTransform } from '@angular/core';

const formatter = new Intl.NumberFormat('es-ES', {
  style: 'currency',
  currency: 'EUR',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

/**
 * Formatea importes en euros al estilo español: 1.299,99 €
 * Uso: {{ producto.precio | price }}
 */
@Pipe({ name: 'price', standalone: true })
export class PricePipe implements PipeTransform {
  transform(value: number | string | null | undefined): string {
    if (value === null || value === undefined || value === '') return '';
    const n = typeof value === 'string' ? Number(value) : value;
    return Number.isFinite(n) ? formatter.format(n) : '';
  }
}
