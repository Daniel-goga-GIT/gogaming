import { AuthService } from '../../core/services/auth';
import { NotificationService } from '../../core/services/notification.service';
import { PricePipe } from '../../shared/pipes/price.pipe';
import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CartService } from '../../core/services/cart.service';
import { TicketService } from '../../core/services/ticket.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { CartItem, CreateCheckoutRequest } from '../../models/types';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [PricePipe, CommonModule, RouterLink, FormsModule, TranslateModule],
  templateUrl: './cart.html',
  styleUrl: './cart.css'
})
export class Cart implements OnInit, OnDestroy {
  private authService = inject(AuthService);
  private notify = inject(NotificationService);
  private translate = inject(TranslateService);
  private route = inject(ActivatedRoute);

  cartItems: CartItem[] = []; // Array local para mostrar los items en la vista
  stockErrors: { [key: number]: string } = {}; // Mapa para controlar errores de stock por producto
  paymentError = '';
  isProcessingPayment = false;
  private subscription: Subscription = new Subscription(); // Para gestionar la suscripción y evitar memory leaks

  constructor(
    private cartService: CartService,
    private ticketService: TicketService,
    private router: Router
  ) {}

  // Se ejecuta al iniciar el componente: se suscribe a los cambios del carrito
  ngOnInit() {
    if (this.route.snapshot.queryParamMap.get('pago') === 'cancelado') {
      // get() espera a que carguen las traducciones (la página viene de Stripe y se carga desde cero).
      this.translate.get(['PAYMENT.CANCELLED_TITLE', 'PAYMENT.CANCELLED_MSG']).subscribe(t => {
        this.notify.info(t['PAYMENT.CANCELLED_TITLE'], { message: t['PAYMENT.CANCELLED_MSG'] });
      });
      this.router.navigate([], { queryParams: {}, replaceUrl: true });
    }

    this.subscription.add(
      this.cartService.cartItems$.subscribe(items => {
        this.cartItems = items; // Actualiza la lista local cuando el servicio emite cambios
      })
    );
  }

  // Se ejecuta al destruir el componente: cancela la suscripción
  ngOnDestroy() {
    this.subscription.unsubscribe(); // Libera memoria al salir
  }

  // Verifica si hay items en el carrito
  get hasItems(): boolean {
    return this.cartItems.length > 0; // True si hay al menos 1 producto
  }

  // Obtiene el total del carrito desde el servicio
  get total(): number {
    return this.cartService.getCartTotal(); // Pide el cálculo al servicio
  }

  // Actualiza la cantidad de un producto usando el servicio
  updateQuantity(productId: number, newQuantity: number) {
    const item = this.cartItems.find(i => i.product.id === productId);
    
    if (item) {
      // Validar stock antes de actualizar
      if (newQuantity > item.product.stock) {
        this.stockErrors[productId] = `¡Limite de productos en Stock superados, hay ${item.product.stock} disponibles!`;
        return; // No actualizamos si supera el stock
      }
      
      // Si bajamos cantidad, borramos el error si existía
      if (this.stockErrors[productId]) {
        delete this.stockErrors[productId];
      }
    }

    this.cartService.updateQuantity(productId, newQuantity); // Delega la lógica al servicio
  }

  // Elimina un producto del carrito usando el servicio
  removeItem(productId: number) {
    this.cartService.removeFromCart(productId); // Delega el borrado al servicio
  }

  getProductDetailRoute(tipoProducto: string, id: number): any[] {
    const routeMap: { [key: string]: string } = {
      'Raton': 'raton',
      'Teclado': 'teclado',
      'Skate': 'skate',
      'Alfombrilla': 'alfombrilla',
      'Auriculares': 'auriculares',
      'Manga': 'manga'
    };
    const segment = routeMap[tipoProducto] || 'raton';
    return [`/productos/${segment}`, id];
  }

  // Procesa la URL de la imagen para asegurar que sea válida
  getImageUrl(url: string): string {
    if (!url) {
      return 'ejemplo.png';
    }
    return url.replace(/\"/g, '').trim();
  }

  /** Lleva al cliente a la página de pago de Stripe (pide login antes si hace falta). */
  completarpago() {
    if (this.isProcessingPayment || !this.hasItems) {
      return;
    }
    if (!this.authService.getToken() || localStorage.getItem('twofactor') !== 'false') {
      sessionStorage.setItem('gogaming_return_url', '/cesta');
      this.notify.info(this.translate.instant('NOTIFY.LOGIN_TO_CHECKOUT'), {
        message: this.translate.instant('NOTIFY.LOGIN_TO_CHECKOUT_MSG'),
      });
      this.router.navigate(['/login']);
      return;
    }

    this.isProcessingPayment = true;
    this.paymentError = '';
    const request: CreateCheckoutRequest = {
      items: this.cartItems.map(item => ({ productId: item.product.id, quantity: item.quantity })),
    };

    this.ticketService.createCheckoutSession(request).subscribe({
      next: ({ url }) => {
        // Se mantiene el estado "redirigiendo" hasta que el navegador cambie de página.
        window.location.assign(url);
      },
      error: (error) => {
        this.isProcessingPayment = false;
        const message = error?.error?.message ?? this.translate.instant('PAYMENT.START_ERROR');
        this.paymentError = message;
        this.notify.error(this.translate.instant('PAYMENT.START_ERROR_TITLE'), { message });
      },
    });
  }
}
