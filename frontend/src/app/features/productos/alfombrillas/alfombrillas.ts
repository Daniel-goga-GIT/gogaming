import { ProductCard } from '../../../shared/product-card/product-card';
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AlfombrillaService } from '../../../core/services/alfombrilla.service';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { RatingService } from '../../../core/services/rating.service';
import { ProductRatingSummary } from '../../../models/types';

@Component({
  selector: 'app-alfombrillas',
  standalone: true,
  imports: [ProductCard, 
    CommonModule,
    FormsModule,
    RouterModule,
    TranslateModule
  ],
  templateUrl: './alfombrillas.html',
})
export class Alfombrillas implements OnInit {
  alfombrillas: any[] = [];
  ratingSummaryMap: Record<number, ProductRatingSummary> = {};
  totalElements: number = 0;
  totalPages: number = 0;
  currentPage: number = 0;
  pageSize: number = 10;
  sortColumn: string = 'nombre';
  sortDirection: string = 'asc';
  filterValue: string = '';
  error: string | null = null;
  loading: boolean = false;

  constructor(
    private alfombrillaService: AlfombrillaService,
    private ratingService: RatingService,
    private router: Router
  ) { }

  ngOnInit() {
    this.fetchAlfombrillas();
  }

  fetchAlfombrillas() {
    this.loading = true;
    this.alfombrillaService.fetchAlfombrillas(this.currentPage, this.pageSize, this.sortColumn, this.sortDirection, this.filterValue).subscribe({
      next: (res: any) => {
        this.alfombrillas = res.content;
        this.loadRatingsForCurrentPage();
        this.totalElements = res.totalElements;
        this.totalPages = res.totalPages;
        this.currentPage = res.number;
        this.pageSize = res.size;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al obtener datos:', err);
        this.loading = false;
        if (err.status === 403) {
          this.router.navigate(['/forbidden']);
        } else {
          this.error = 'Error al cargar las alfombrillas';
        }
      }
    });
  }

  handlePageChange(page: number) {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.fetchAlfombrillas();
    }
  }

  handleSort(column: string) {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
    this.fetchAlfombrillas();
  }

  applyFilter() {
    this.currentPage = 0;
    this.fetchAlfombrillas();
  }

  getImageUrl(url: string): string {
    if (!url) return 'ejemplo.png';
    return url.replace(/\"/g, '').trim();
  }

  loadRatingsForCurrentPage(): void {
    this.alfombrillas.forEach((alfombrilla) => {
      this.ratingService.getProductSummary(alfombrilla.id).subscribe({
        next: (summary) => {
          this.ratingSummaryMap[alfombrilla.id] = summary;
        },
        error: () => {
          this.ratingSummaryMap[alfombrilla.id] = {
            productId: alfombrilla.id,
            averageRating: 0,
            totalReviews: 0,
            oneStar: 0,
            twoStars: 0,
            threeStars: 0,
            fourStars: 0,
            fiveStars: 0
          };
        }
      });
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
