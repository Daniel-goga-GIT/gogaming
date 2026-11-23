import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { UserService } from '../../core/services/user';
import { AuthService } from '../../core/services/auth';
import { TicketService, TicketResponse } from '../../core/services/ticket.service';
import { RatingService } from '../../core/services/rating.service';
import { TranslateModule } from '@ngx-translate/core';
import { RatingDTO, UserDTO } from '../../models/types';
import { environment } from '../../../environments/environments';

@Component({
  selector: 'app-me',
  imports: [CommonModule, RouterLink, TranslateModule],
  templateUrl: './me.html',
  styleUrl: './me.css'
})
export class Me implements OnInit {
  user: UserDTO = new UserDTO();
  email: string | null = null;
  tickets: TicketResponse[] = [];
  reviews: RatingDTO[] = [];
  loadingTickets: boolean = false;
  loadingReviews: boolean = false;
  productImages: { [productId: number]: string } = {};
  productNames: { [productId: number]: string } = {};

  constructor(
    private http: HttpClient,
    private userService: UserService,
    private authService: AuthService,
    private ticketService: TicketService,
    private ratingService: RatingService
  ) {}

  ngOnInit(): void {
    // Obtener el email del usuario autenticado
    // Pero lo censuramos para no exponerlo
   this.email = this.authService.getUsername()?.replace(/^(.)[^@]+/, '$1****') ?? '';
    this.userService.fetchUser().subscribe({
      next: (res: UserDTO) => {
        this.user.firstName = res.firstName ?? '';
        this.user.lastName = res.lastName ?? '';
        this.user.image = res.image ? `${environment.apiUrl}/v1/users/image/${res.image}` : 'https://cdn-icons-png.flaticon.com/512/6326/6326055.png';
        this.user.username = res.username ?? '';
      },
      error: () => {
        // Silencioso: si falla, mantenemos valores por defecto
      }
    });

    // Cargar historial de tickets
    this.loadUserTickets();
    this.loadUserReviews();
  }

  loadUserTickets(): void {
    this.loadingTickets = true;
    this.ticketService.getUserTickets().subscribe({
      next: (tickets: TicketResponse[]) => {
        this.tickets = tickets.sort((a, b) => new Date(b.fecha).getTime() - new Date(a.fecha).getTime());
        this.loadProductDetails();
        this.loadingTickets = false;
      },
      error: (err) => {
        console.error('Error al cargar tickets', err);
        this.loadingTickets = false;
      }
    });
  }

  loadUserReviews(): void {
    this.loadingReviews = true;
    this.ratingService.getMyReviews().subscribe({
      next: (reviews: RatingDTO[]) => {
        this.reviews = reviews;
        this.loadProductDetails();
        this.loadingReviews = false;
      },
      error: (err) => {
        console.error('Error al cargar resenas del usuario', err);
        this.loadingReviews = false;
      }
    });
  }

  private loadProductDetails(): void {
    this.tickets.forEach(ticket => {
      ticket.lineas.forEach(linea => {
        this.productImages[linea.productId] = this.getImageUrl(linea.productImage);
        this.productNames[linea.productId] = linea.productName;
      });
    });

    this.reviews.forEach(review => {
      const id = review.productId;
      if (this.productNames[id]) return;

      this.http.get<any>(`${environment.apiUrl}/v1/productos/${id}`).subscribe({
        next: (product) => {
          this.productImages[id] = this.getImageUrl(product.image);
          this.productNames[id] = product.nombre;
        }
      });
    });
  }

  getProductImage(productId: number): string {
    return this.productImages[productId] || 'https://cdn-icons-png.flaticon.com/512/3144/3144456.png';
  }

  getProductName(productId: number): string {
    return this.productNames[productId] || `Producto #${productId}`;
  }

  getStarsArray(): number[] {
    return [1, 2, 3, 4, 5];
  }

  private getImageUrl(url?: string | null): string {
    if (!url) {
      return 'https://cdn-icons-png.flaticon.com/512/3144/3144456.png';
    }
    return url.replace(/\"/g, '').trim();
  }

  //Metodo para subir la imagen de perfil
  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      this.userService.uploadImage(file).subscribe({
        next: (res: any) => {
          // Recargar datos del usuario para asegurar consistencia
          this.userService.fetchUser().subscribe((userRes: UserDTO) => {
             this.user.image = userRes.image ? `${environment.apiUrl}/v1/users/image/${userRes.image}` : 'https://cdn-icons-png.flaticon.com/512/6326/6326055.png';
          });
          window.location.reload();
        },
        error: (err) => {
          console.error('Error al subir la imagen', err);
        }
      });
    }
  }

  descargarPDF() {
    alert('Proximamente se implementara la descarga de PDFs');
  }
}
