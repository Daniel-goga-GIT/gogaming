import { PricePipe } from '../../../../shared/pipes/price.pipe';
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuricularesService } from '../../../../core/services/auriculares.service';
import { TranslateModule } from '@ngx-translate/core';
import { CreateRatingRequest, ProductRatingSummary, AuricularesDTO, RatingDTO } from '../../../../models/types';
import { CartFeedbackService } from '../../../../core/services/cart-feedback.service';
import { RatingService } from '../../../../core/services/rating.service';

@Component({
  selector: 'app-auricular-detail',
  standalone: true,
  imports: [PricePipe, CommonModule, RouterModule, FormsModule, TranslateModule],
  templateUrl: './auricular-detail.html',
  styleUrls: ['./auricular-detail.css']
})
export class AuricularDetail implements OnInit {
  auricular: AuricularesDTO | null = null;
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
    private auricularesService: AuricularesService,
    private ratingService: RatingService,
    private cartFeedback: CartFeedbackService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.fetchAuricular(Number(id));
    } else {
      this.error = 'No se ha proporcionado un ID de producto';
      this.loading = false;
    }
  }

  fetchAuricular(id: number): void {
    this.loading = true;
    this.auricularesService.getAuricularesById(id).subscribe({
      next: (res: AuricularesDTO) => {
        this.auricular = res;
        this.loadReviews();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching auricular detail:', err);
        this.error = 'Error al cargar el detalle del auricular';
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
    if (!this.auricular) return;

    this.loadingReviews = true;
    this.reviewError = null;

    this.ratingService
      .getProductReviews(this.auricular.id)
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
    if (!this.auricular || !this.canSubmitReview()) {
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

    this.ratingService.createReview(this.auricular.id, body).subscribe({
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
    if (this.auricular) {
      this.cartFeedback.add(this.auricular);
    }
  }
}
