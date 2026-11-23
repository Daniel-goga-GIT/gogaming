import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { environment } from '../../../environments/environments';
import { AuthService } from './auth';

export interface LineaTicketResponse {
  id: number;
  productId: number;
  productName: string;
  productImage: string | null;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface TicketResponse {
  id: number;
  userId: number;
  numeroTicket: string;
  fecha: string;
  total: number;
  metodoPago: string;
  estado: string;
  lineas: LineaTicketResponse[];
}

import { CheckoutSessionResponse, CreateCheckoutRequest } from '../../models/types';

@Injectable({
  providedIn: 'root'
})
export class TicketService {

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  getUserTickets(): Observable<TicketResponse[]> {
    const token = this.authService.getToken();
    if (!token) {
      return throwError(() => new Error('Unauthorized'));
    }

    return this.http.get<TicketResponse[]>(`${environment.apiUrl}/v1/payments/tickets`, {
      headers: new HttpHeaders({
        Authorization: `Bearer ${token}`
      })
    });
  }

  getAllTickets(): Observable<TicketResponse[]> {
    const token = this.authService.getToken();
    if (!token) return throwError(() => new Error('Unauthorized'));
    return this.http.get<TicketResponse[]>(`${environment.apiUrl}/v1/payments/tickets/all`, {
      headers: new HttpHeaders({ Authorization: `Bearer ${token}` })
    });
  }

  updateEstado(ticketId: number, estado: string): Observable<TicketResponse> {
    const token = this.authService.getToken();
    if (!token) return throwError(() => new Error('Unauthorized'));
    return this.http.patch<TicketResponse>(`${environment.apiUrl}/v1/payments/tickets/${ticketId}/estado`, { estado }, {
      headers: new HttpHeaders({ Authorization: `Bearer ${token}` })
    });
  }

  /** Crea la sesión de pago de Stripe; hay que redirigir a `url`. */
  createCheckoutSession(request: CreateCheckoutRequest): Observable<CheckoutSessionResponse> {
    const token = this.authService.getToken();
    if (!token) return throwError(() => new Error('Unauthorized'));
    return this.http.post<CheckoutSessionResponse>(`${environment.apiUrl}/v1/payments/checkout-session`, request, {
      headers: new HttpHeaders({ Authorization: `Bearer ${token}` })
    });
  }

  /** Confirma el pago al volver de Stripe. status 200 = pedido creado; 202 = pago aún pendiente. */
  confirmCheckoutSession(sessionId: string): Observable<HttpResponse<TicketResponse | { status: string }>> {
    const token = this.authService.getToken();
    if (!token) return throwError(() => new Error('Unauthorized'));
    return this.http.get<TicketResponse | { status: string }>(
      `${environment.apiUrl}/v1/payments/checkout-session/${encodeURIComponent(sessionId)}`,
      { headers: new HttpHeaders({ Authorization: `Bearer ${token}` }), observe: 'response' }
    );
  }
}
