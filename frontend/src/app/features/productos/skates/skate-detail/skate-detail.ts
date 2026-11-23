import { PricePipe } from '../../../../shared/pipes/price.pipe';
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SkateService } from '../../../../core/services/skate.service';
import { TranslateModule } from '@ngx-translate/core';
import { CreateRatingRequest, ProductRatingSummary, SkateDTO, RatingDTO } from '../../../../models/types';
import { CartFeedbackService } from '../../../../core/services/cart-feedback.service';
import { RatingService } from '../../../../core/services/rating.service';

@Component({
  selector: 'app-skate-detail',
  standalone: true,
  imports: [PricePipe, CommonModule, RouterModule, FormsModule, TranslateModule],
  templateUrl: './skate-detail.html',
  styleUrls: ['./skate-detail.css']
})
export class SkateDetail implements OnInit {
  skate: SkateDTO | null = null;
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
    private skateService: SkateService,
    private ratingService: RatingService,
    private cartFeedback: CartFeedbackService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.fetchSkate(Number(id));
    } else {
      this.error = 'No se ha proporcionado un ID de producto';
      this.loading = false;
    }
  }

  fetchSkate(id: number): void {
    this.loading = true;
    this.skateService.getSkateById(id).subscribe({
      next: (res: SkateDTO) => {
        this.skate = res;
        this.loadReviews();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching skate detail:', err);
        this.error = 'Error al cargar el detalle del skate';
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
    if (!this.skate) return;

    this.loadingReviews = true;
    this.reviewError = null;

    this.ratingService
      .getProductReviews(this.skate.id)
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

  get isLoggedIn(): boolean {
    return !!localStorage.getItem('gogaming_token') && localStorage.getItem('twofactor') === 'false';
  }

  rememberReturn(): void {
    sessionStorage.setItem('gogaming_return_url', this.router.url);
  }

  canSubmitReview(): boolean {
    return this.formRating >= 1 && this.formRating <= 5;
  }

  submitReview(): void {
    if (!this.skate || !this.canSubmitReview()) {
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

    this.ratingService.createReview(this.skate.id, body).subscribe({
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
    if (this.skate) {
      this.cartFeedback.add(this.skate);
    }
  }
}
