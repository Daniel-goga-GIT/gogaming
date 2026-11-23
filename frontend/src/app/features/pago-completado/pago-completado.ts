import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { timer } from 'rxjs';
import { CartService } from '../../core/services/cart.service';
import { TicketResponse, TicketService } from '../../core/services/ticket.service';
import { PricePipe } from '../../shared/pipes/price.pipe';

type State = 'confirming' | 'success' | 'pending' | 'error';

/**
 * Página a la que Stripe devuelve al cliente tras pagar (?session_id=cs_...).
 * Confirma el pago con el backend; solo entonces vacía la cesta y muestra el pedido.
 */
@Component({
  selector: 'app-pago-completado',
  standalone: true,
  imports: [RouterModule, TranslateModule, PricePipe],
  templateUrl: './pago-completado.html',
  styleUrls: ['./pago-completado.css'],
})
export class PagoCompletado implements OnInit {
  private route = inject(ActivatedRoute);
  private ticketService = inject(TicketService);
  private cartService = inject(CartService);
  private destroyRef = inject(DestroyRef);

  /** Reintentos mientras Stripe todavía no marca la sesión como pagada. */
  private static readonly MAX_ATTEMPTS = 8;
  private static readonly RETRY_MS = 1500;

  protected readonly state = signal<State>('confirming');
  protected readonly ticket = signal<TicketResponse | null>(null);

  ngOnInit(): void {
    const sessionId = this.route.snapshot.queryParamMap.get('session_id');
    if (!sessionId) {
      this.state.set('error');
      return;
    }
    this.confirm(sessionId, 1);
  }

  private confirm(sessionId: string, attempt: number): void {
    this.ticketService
      .confirmCheckoutSession(sessionId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: response => {
          if (response.status === 200) {
            this.ticket.set(response.body as TicketResponse);
            this.cartService.clearCart();
            this.state.set('success');
          } else if (attempt < PagoCompletado.MAX_ATTEMPTS) {
            timer(PagoCompletado.RETRY_MS)
              .pipe(takeUntilDestroyed(this.destroyRef))
              .subscribe(() => this.confirm(sessionId, attempt + 1));
          } else {
            // Pagos asíncronos (p. ej. transferencias): el webhook creará el pedido más tarde.
            this.cartService.clearCart();
            this.state.set('pending');
          }
        },
        error: () => this.state.set('error'),
      });
  }
}
