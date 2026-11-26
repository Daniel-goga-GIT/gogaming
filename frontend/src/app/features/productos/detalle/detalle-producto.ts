import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { Categoria, CampoProducto, categoriaPorSlug, urlImagen } from '../../../core/catalogo';
import { CartFeedbackService } from '../../../core/services/cart-feedback.service';
import { ProductoService } from '../../../core/services/producto.service';
import { RatingService } from '../../../core/services/rating.service';
import { ProductRatingSummary, ProductoDTO, RatingDTO } from '../../../models/types';
import { PricePipe } from '../../../shared/pipes/price.pipe';

type Estado = 'cargando' | 'listo' | 'no-encontrado' | 'error';

/** Ficha de un producto de cualquier categoría: /productos/{categoria}/{id}. */
@Component({
  selector: 'app-detalle-producto',
  standalone: true,
  imports: [PricePipe, NgClass, RouterModule, FormsModule, TranslateModule],
  templateUrl: './detalle-producto.html',
  styleUrl: './detalle-producto.css',
})
export class DetalleProducto implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private productoService = inject(ProductoService);
  private ratingService = inject(RatingService);
  private cartFeedback = inject(CartFeedbackService);
  private destroyRef = inject(DestroyRef);

  protected readonly urlImagen = urlImagen;
  protected readonly estrellas = [1, 2, 3, 4, 5];

  protected categoria: Categoria | undefined;
  protected readonly estado = signal<Estado>('cargando');
  protected readonly producto = signal<ProductoDTO | null>(null);
  protected readonly summary = signal<ProductRatingSummary | null>(null);
  protected readonly reviews = signal<RatingDTO[]>([]);
  protected readonly loadingReviews = signal(false);
  /** Claves i18n de los avisos de la sección de reseñas (o el texto del backend). */
  protected readonly reviewError = signal<string | null>(null);
  protected readonly reviewSuccess = signal(false);
  protected readonly submittingReview = signal(false);

  protected formRating = 5;
  protected formComment = '';

  ngOnInit(): void {
    // Se escucha paramMap porque se puede pasar de una ficha a otra sin destruir el componente.
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(params => {
      this.categoria = categoriaPorSlug(params.get('categoria'));
      const id = Number(params.get('id'));
      if (!this.categoria || !Number.isInteger(id)) {
        this.estado.set('no-encontrado');
        return;
      }
      this.cargar(this.categoria, id);
    });
  }

  private cargar(categoria: Categoria, id: number): void {
    this.estado.set('cargando');
    this.producto.set(null);
    this.reviewSuccess.set(false);
    this.productoService.obtener(categoria.slug, id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: producto => {
        this.producto.set(producto);
        this.estado.set('listo');
        this.cargarReviews();
      },
      error: err => this.estado.set(err.status === 404 ? 'no-encontrado' : 'error'),
    });
  }

  /** Valor de una especificación con su unidad, o un guion si no hay dato. */
  protected valor(campo: CampoProducto): string {
    const valor = (this.producto() as unknown as Record<string, unknown>)?.[campo.clave];
    if (valor === null || valor === undefined || valor === '') {
      return '—';
    }
    return campo.unidad ? `${valor} ${campo.unidad}` : String(valor);
  }

  protected get media(): number {
    return this.summary()?.averageRating ?? 0;
  }

  private cargarReviews(): void {
    const producto = this.producto();
    if (!producto) return;
    this.loadingReviews.set(true);
    this.reviewError.set(null);
    this.ratingService.getProductReviews(producto.id).subscribe({
      next: response => {
        this.summary.set(response.summary);
        this.reviews.set(response.reviews);
        this.loadingReviews.set(false);
      },
      error: () => {
        this.reviewError.set('RATINGS.LOAD_ERROR');
        this.loadingReviews.set(false);
      },
    });
  }

  protected get isLoggedIn(): boolean {
    return !!localStorage.getItem('gogaming_token') && localStorage.getItem('twofactor') === 'false';
  }

  protected rememberReturn(): void {
    sessionStorage.setItem('gogaming_return_url', this.router.url);
  }

  protected submitReview(): void {
    const producto = this.producto();
    const rating = Number(this.formRating);
    if (!producto || rating < 1 || rating > 5) {
      this.reviewError.set('RATINGS.INVALID_SCORE');
      return;
    }

    this.submittingReview.set(true);
    this.reviewError.set(null);
    this.reviewSuccess.set(false);

    this.ratingService.createReview(producto.id, { calificacion: rating, contenido: this.formComment.trim() }).subscribe({
      next: () => {
        this.reviewSuccess.set(true);
        this.formRating = 5;
        this.formComment = '';
        this.submittingReview.set(false);
        this.cargarReviews();
      },
      error: err => {
        this.reviewError.set(err?.status === 409 ? 'RATINGS.ALREADY_REVIEWED' : 'RATINGS.SAVE_ERROR');
        this.submittingReview.set(false);
      },
    });
  }

  protected addToCart(): void {
    const producto = this.producto();
    if (producto) {
      this.cartFeedback.add(producto);
    }
  }
}
