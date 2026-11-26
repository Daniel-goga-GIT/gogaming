import { ProductCard } from '../../shared/product-card/product-card';
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ProductRatingSummary, ProductoDTO } from '../../models/types';
import { ProductoService } from '../../core/services/producto.service';
import { rutaProducto, urlImagen } from '../../core/catalogo';
import { TranslateModule } from '@ngx-translate/core';
import { RatingService } from '../../core/services/rating.service';

@Component({
  selector: 'app-home',
  imports: [ProductCard, CommonModule, RouterModule, TranslateModule],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnInit {
  ratones: ProductoDTO[] = [];
  protected readonly rutaProducto = rutaProducto;
  ratingSummaryMap: Record<number, ProductRatingSummary> = {};

  constructor(
    private productoService: ProductoService,
    private ratingService: RatingService
  ) {}

  ngOnInit(): void {
    this.productoService.portada().subscribe({
      next: (data) => {
        this.ratones = data;
        this.loadRatingsForHome();
      },
      error: (err) => {
        console.error('Error fetching ratones', err);
      }
    });
  }

  getImageUrl(url: string): string {
    return urlImagen(url);
  }

  loadRatingsForHome(): void {
    this.ratingService.getSummaries(this.ratones.map(r => r.id)).subscribe({
      next: resumenes => (this.ratingSummaryMap = resumenes),
      error: () => (this.ratingSummaryMap = {}),
    });
  }

  getRatingValue(productId: number): number {
    return this.ratingSummaryMap[productId]?.averageRating ?? 0;
  }

  getRating(productId: number): string {
    return this.getRatingValue(productId).toFixed(1);
  }

  getOpiniones(productId: number): number {
    return this.ratingSummaryMap[productId]?.totalReviews ?? 0;
  }

  getStarsArray(): number[] {
    return [1, 2, 3, 4, 5];
  }
}
