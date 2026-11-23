import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CartService } from '../../core/services/cart.service';
import { TicketService } from '../../core/services/ticket.service';
import { TranslateModule } from '@ngx-translate/core';
import { CartItem, CreateCheckoutRequest } from '../../models/types';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, TranslateModule],
  templateUrl: './cart.html',
  styleUrl: './cart.css'
})
export class Cart implements OnInit, OnDestroy {
  cartItems: CartItem[] = []; // Array local para mostrar los items en la vista
  stockErrors: { [key: number]: string } = {}; // Mapa para controlar errores de stock por producto
  paymentError = '';
  isProcessingPayment = false;
  selectedPaymentMethod = 'Tarjeta'; // Método de pago por defecto
  showPaymentModal = false;
  cardDetails = {
    number: '',
    expiry: '',
    cvv: '',
    name: ''
  };
  private subscription: Subscription = new Subscription(); // Para gestionar la suscripción y evitar memory leaks

  constructor(
    private cartService: CartService,
    private ticketService: TicketService,
    private router: Router
  ) {}

  // Se ejecuta al iniciar el componente: se suscribe a los cambios del carrito
  ngOnInit() {
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

  // Formateadores para el modal de pago
  formatCardNumber(event: any) {
    let value = event.target.value.replace(/\D/g, '');
    if (value.length > 16) {
      value = value.slice(0, 16);
    }
    
    const parts = [];
    for (let i = 0; i < value.length; i += 4) {
      parts.push(value.slice(i, i + 4));
    }
    const formatted = parts.join(' ');
    this.cardDetails.number = formatted;
    event.target.value = formatted;
  }

  formatExpiry(event: any) {
    let value = event.target.value.replace(/\D/g, '');
    if (value.length > 4) value = value.slice(0, 4);
    
    let formatted = value;
    if (value.length >= 3) {
      formatted = value.slice(0, 2) + '/' + value.slice(2);
    }
    this.cardDetails.expiry = formatted;
    event.target.value = formatted;
  }

  formatCVV(event: any) {
    let value = event.target.value.replace(/\D/g, '');
    if (value.length > 3) {
      value = value.slice(0, 3);
    }
    this.cardDetails.cvv = value;
    event.target.value = value;
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

  completarpago() {
    if (this.isProcessingPayment || !this.hasItems) {
      return;
    }
    this.showPaymentModal = true;
  }

  closePaymentModal() {
    if (this.isProcessingPayment) {
      return;
    }
    this.showPaymentModal = false;
  }

  confirmarPago() {
    if (this.isProcessingPayment) {
      return;
    }

    this.isProcessingPayment = true;
    this.paymentError = '';

    // Simular procesamiento de 5 segundos
    setTimeout(() => {
      const checkoutRequest: CreateCheckoutRequest = {
        items: this.cartItems.map(item => ({
          productId: item.product.id,
          quantity: item.quantity
        })),
        metodoPago: this.selectedPaymentMethod
      };

      this.ticketService.createTicketFromCheckout(checkoutRequest).subscribe({
        next: (response) => {
          console.log('Pago completado con éxito:', response);
          this.isProcessingPayment = false;
          this.showPaymentModal = false;
          // enviamos al pago
          this.router.navigate(['/pago-completado']);
        },
        error: (error) => {
          console.error('Error al procesar el pago:', error);
          this.paymentError = typeof error.error === 'string' ? error.error : 'Error al procesar el pago. Inténtelo de nuevo.';
          this.isProcessingPayment = false;
        }
      });
    }, 5000);
  }
}
