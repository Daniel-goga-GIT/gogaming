import { Injectable, inject } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { ProductoDTO } from '../../models/types';
import { CartService } from './cart.service';
import { NotificationService } from './notification.service';

/**
 * Añade un producto a la cesta y muestra el aviso adecuado
 * (añadido, agotado o sin más stock). Lo usan todas las fichas de producto.
 */
@Injectable({ providedIn: 'root' })
export class CartFeedbackService {
  private cart = inject(CartService);
  private notify = inject(NotificationService);
  private translate = inject(TranslateService);

  add(product: ProductoDTO): boolean {
    const result = this.cart.addToCart(product);
    const name = [product.marca, product.nombre].filter(Boolean).join(' ');

    if (result.ok) {
      const quantity = this.cart.getItemQuantity(product.id);
      this.notify.success(this.t('NOTIFY.CART_ADDED'), {
        message: quantity > 1 ? this.t('NOTIFY.CART_ADDED_QTY', { name, quantity }) : name,
        image: product.image,
        action: { label: this.t('NOTIFY.VIEW_CART'), route: '/cesta' },
      });
      return true;
    }

    if (result.reason === 'out_of_stock') {
      this.notify.warning(this.t('NOTIFY.OUT_OF_STOCK'), {
        message: this.t('NOTIFY.OUT_OF_STOCK_MSG', { name }),
      });
    } else {
      this.notify.warning(this.t('NOTIFY.STOCK_LIMIT'), {
        message: this.t('NOTIFY.STOCK_LIMIT_MSG', { available: result.available }),
        action: { label: this.t('NOTIFY.VIEW_CART'), route: '/cesta' },
      });
    }
    return false;
  }

  private t(key: string, params?: Record<string, unknown>): string {
    return this.translate.instant(key, params);
  }
}
