import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CreateRatingRequest,
  ProductRatingSummary,
  ProductReviewsResponse,
  RatingDTO
} from '../../models/types';
import { environment } from '../../../environments/environments';
import { AuthService } from './auth';

@Injectable({
  providedIn: 'root'
})
export class RatingService {
  private apiUrl = `${environment.apiUrl}/v1/productos/raton`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  getProductReviews(productId: number): Observable<ProductReviewsResponse> {
    return this.http.get<ProductReviewsResponse>(
      `${this.apiUrl}/${productId}/resenas`,
      { headers: this.buildHeaders() }
    );
  }

  getProductSummary(productId: number): Observable<ProductRatingSummary> {
    return this.http.get<ProductRatingSummary>(
      `${this.apiUrl}/${productId}/resenas/summary`,
      { headers: this.buildHeaders() }
    );
  }

  createReview(productId: number, body: CreateRatingRequest): Observable<RatingDTO> {
    return this.http.post<RatingDTO>(
      `${this.apiUrl}/${productId}/resenas`,
      body,
      { headers: this.buildHeaders(true) }
    );
  }

  getMyReviews(): Observable<RatingDTO[]> {
    return this.http.get<RatingDTO[]>(
      `${environment.apiUrl}/v1/users/me/resenas`,
      { headers: this.buildHeaders(true) }
    );
  }

  private buildHeaders(requireAuth: boolean = false): HttpHeaders {
    const token = this.authService.getToken();

    if (!token) {
      if (requireAuth) {
        return new HttpHeaders({ 'Content-Type': 'application/json' });
      }
      return new HttpHeaders();
    }

    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }
}
