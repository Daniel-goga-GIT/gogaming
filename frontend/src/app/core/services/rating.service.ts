import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, of } from 'rxjs';
import { environment } from '../../../environments/environments';
import { CreateRatingRequest, ProductRatingSummary, ProductReviewsResponse, RatingDTO } from '../../models/types';

/** Reseñas y valoraciones de productos (el token lo añade el interceptor). */
@Injectable({ providedIn: 'root' })
export class RatingService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/v1/productos`;

  getProductReviews(productId: number): Observable<ProductReviewsResponse> {
    return this.http.get<ProductReviewsResponse>(`${this.apiUrl}/${productId}/resenas`);
  }

  getProductSummary(productId: number): Observable<ProductRatingSummary> {
    return this.http.get<ProductRatingSummary>(`${this.apiUrl}/${productId}/resenas/summary`);
  }

  /** Resumen de varios productos con una sola petición: { [id]: resumen }. */
  getSummaries(productIds: number[]): Observable<Record<number, ProductRatingSummary>> {
    if (productIds.length === 0) {
      return of({});
    }
    const params = new HttpParams().set('ids', productIds.join(','));
    return this.http.get<Record<number, ProductRatingSummary>>(`${this.apiUrl}/resenas/resumen`, { params });
  }

  createReview(productId: number, body: CreateRatingRequest): Observable<RatingDTO> {
    return this.http.post<RatingDTO>(`${this.apiUrl}/${productId}/resenas`, body);
  }

  getMyReviews(): Observable<RatingDTO[]> {
    return this.http.get<RatingDTO[]>(`${environment.apiUrl}/v1/users/me/resenas`);
  }
}
