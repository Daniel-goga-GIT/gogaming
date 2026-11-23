import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { ProductoDTO, CartItem } from '../../models/types';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  // Clave para guardar en localStorage
  private cartKey = 'gogaming_cart';
  // BehaviorSubject para mantener el estado actual del carrito y emitir cambios
  private cartItemsSubject = new BehaviorSubject<CartItem[]>([]);
  
  // Observable público para que los componentes se suscriban a los cambios del carrito
  cartItems$ = this.cartItemsSubject.asObservable();

  constructor() {
    this.loadCart(); // Cargar carrito al iniciar el servicio
  }

  // Carga los items del carrito desde localStorage
  private loadCart() {
    const savedCart = localStorage.getItem(this.cartKey); // Intenta obtener datos guardados
    if (savedCart) {
      try {
        const items = JSON.parse(savedCart); // Convierte el texto guardado a objetos
        this.cartItemsSubject.next(items); // Emite los items recuperados
      } catch (e) {
        console.error('Error parsing cart from localStorage', e);
        this.cartItemsSubject.next([]); // Si hay error, inicia vacío
      }
    }
  }

  // Guarda los items en localStorage y actualiza el Subject
  private saveCart(items: CartItem[]) {
    localStorage.setItem(this.cartKey, JSON.stringify(items)); // Guarda en navegador
    this.cartItemsSubject.next(items); // Notifica a los suscriptores
  }

  getItemQuantity(productId: number): number {
    const item = this.cartItemsSubject.value.find(i => i.product.id === productId);
    return item?.quantity ?? 0;
  }

  // Añade un producto al carrito o incrementa su cantidad si ya existe
  addToCart(
    product: ProductoDTO
  ): { ok: true } | { ok: false; reason: 'out_of_stock' } | { ok: false; reason: 'stock'; available: number } {
    if (product.stock <= 0) {
      return { ok: false, reason: 'out_of_stock' };
    }

    const currentItems = this.cartItemsSubject.value; // Obtiene items actuales
    const existingItem = currentItems.find(item => item.product.id === product.id); // Busca si ya existe

    if (existingItem) {
      if (existingItem.quantity >= product.stock) {
        return { ok: false, reason: 'stock', available: product.stock };
      }

      existingItem.quantity += 1; // Si existe, suma 1 a la cantidad
      this.saveCart([...currentItems]); // Guarda actualizando el array
    } else {
      this.saveCart([...currentItems, { product, quantity: 1 }]); // Si no, lo añade nuevo
    }

    return { ok: true };
  }

  // Elimina un producto del carrito por su ID
  removeFromCart(productId: number) {
    const currentItems = this.cartItemsSubject.value; // Obtiene items actuales
    // Filtra para dejar solo los que NO tengan ese ID
    const updatedItems = currentItems.filter(item => item.product.id !== productId);
    this.saveCart(updatedItems); // Guarda la lista filtrada
  }

  // Actualiza la cantidad de un producto. Si es <= 0, lo elimina
  updateQuantity(productId: number, quantity: number) {
    const currentItems = this.cartItemsSubject.value;
    const item = currentItems.find(item => item.product.id === productId); // Busca el item
    
    if (item) {
      if (quantity <= 0) {
        this.removeFromCart(productId); // Si cantidad es 0 o menos, borra
      } else {
        item.quantity = Math.min(quantity, item.product.stock); // Actualiza la cantidad
        this.saveCart([...currentItems]); // Guarda los cambios
      }
    }
  }

  // Vacía el carrito completamente
  clearCart() {
    this.saveCart([]); // Guarda un array vacío
  }

  // Calcula el precio total de todos los items en el carrito
  getCartTotal(): number {
    return this.cartItemsSubject.value.reduce((total, item) => {
      // Suma: acumulado + (precio * cantidad)
      return total + (item.product.precio * item.quantity);
    }, 0);
  }
  
  // Calcula la cantidad total de productos (suma de cantidades)
  getCartCount(): number {
    return this.cartItemsSubject.value.reduce((count, item) => {
      // Suma: acumulado + cantidad del item
      return count + item.quantity;
    }, 0);
  }
}
