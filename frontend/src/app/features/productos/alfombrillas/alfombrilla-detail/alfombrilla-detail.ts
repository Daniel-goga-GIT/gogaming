import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AlfombrillaService } from '../../../../core/services/alfombrilla.service';
import { TranslateModule } from '@ngx-translate/core';
import { CreateRatingRequest, ProductRatingSummary, AlfombrillaDTO, RatingDTO } from '../../../../models/types';
import { CartService } from '../../../../core/services/cart.service';
import { RatingService } from '../../../../core/services/rating.service';

@Component({
  selector: 'app-alfombrilla-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, TranslateModule],
  templateUrl: './alfombrilla-detail.html',
  styleUrls: ['./alfombrilla-detail.css']
})
export class AlfombrillaDetail implements OnInit {
  alfombrilla: AlfombrillaDTO | null = null;
  summary: ProductRatingSummary | null = null;
  reviews: RatingDTO[] = [];
  loading: boolean = true;
  loadingReviews: boolean = false;
  error: string | null = null;
  reviewError: string | null = null;
  reviewSuccess: string | null = null;

  formRating: number = 5;
  formComment: string = '';
  submittingReview: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private alfombrillaService: AlfombrillaService,
    private ratingService: RatingService,
    private cartService: CartService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.fetchAlfombrilla(Number(id));
    } else {
      this.error = 'No se ha proporcionado un ID de producto';
      this.loading = false;
    }
  }

  fetchAlfombrilla(id: number): void {
    this.loading = true;
    this.alfombrillaService.getAlfombrillaById(id).subscribe({
      next: (res: AlfombrillaDTO) => {
        this.alfombrilla = res;
        this.loadReviews();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching alfombrilla detail:', err);
        this.error = 'Error al cargar el detalle de la alfombrilla';
        this.loading = false;
      }
    });
  }

  getImageUrl(url: string | undefined): string {
    if (!url) return 'ejemplo.png';
    return url.replace(/\"/g, '').trim();
  }

  getStarsArray(): number[] {
    return [1, 2, 3, 4, 5];
  }

  getRatingValue(): number {
    return this.summary?.averageRating ?? 0;
  }

  getRating(): string {
    return this.getRatingValue().toFixed(1);
  }

  loadReviews(): void {
    if (!this.alfombrilla) return;

    this.loadingReviews = true;
    this.reviewError = null;

    this.ratingService
      .getProductReviews(this.alfombrilla.id)
      .subscribe({
        next: (response) => {
          this.summary = response.summary;
          this.reviews = response.reviews;
          this.loadingReviews = false;
        },
        error: (err) => {
          console.error('Error loading reviews:', err);
          this.reviewError = 'Error al cargar valoraciones';
          this.loadingReviews = false;
        }
      });
  }

  canSubmitReview(): boolean {
    return this.formRating >= 1 && this.formRating <= 5;
  }

  submitReview(): void {
    if (!this.alfombrilla || !this.canSubmitReview()) {
      this.reviewError = 'La reseña debe tener entre 1 y 5 estrellas.';
      return;
    }

    const body: CreateRatingRequest = {
      calificacion: this.formRating,
      contenido: this.formComment.trim()
    };

    this.submittingReview = true;
    this.reviewError = null;
    this.reviewSuccess = null;

    this.ratingService.createReview(this.alfombrilla.id, body).subscribe({
      next: () => {
        this.reviewSuccess = 'Reseña publicada correctamente.';
        this.formRating = 5;
        this.formComment = '';
        this.submittingReview = false;
        this.loadReviews();
      },
      error: (err) => {
        console.error('Error saving review:', err);
        this.reviewError = typeof err?.error === 'string' ? err.error : 'No se pudo guardar la reseña';
        this.submittingReview = false;
      }
    });
  }

  getStock(stock: number): string {
    if (stock > 10) {
      return 'En stock (' + stock + ')';
    } else if (stock > 0) {
      return '¡Últimas ' + stock + ' unidades!';
    } else {
      return 'Agotado';
    }
  }

  addToCart(): void {
    if (this.alfombrilla) {
      this.cartService.addToCart(this.alfombrilla);
      alert('Alfombrilla añadida correctamente al carrito.');
    }
  }
}
