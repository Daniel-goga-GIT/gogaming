import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SkateDTO } from '../../models/types';
import { environment } from '../../../environments/environments';
import { AuthService } from './auth';

@Injectable({
  providedIn: 'root'
})
export class SkateService {
  private apiUrl = `${environment.apiUrl}/v1/productos/skate`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  fetchSkates(page: number, size: number, sortColumn: string, sortDirection: string, nombre?: string): Observable<any> {
    const token = this.authService.getToken();

    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', `${sortColumn},${sortDirection}`);

    if (nombre) {
      params = params.set('nombre', nombre);
    }

    const headers = token ? new HttpHeaders().set('Authorization', `Bearer ${token}`) : new HttpHeaders();

    return this.http.get<any>(this.apiUrl, { params, headers });
  }

  getSkateById(id: number): Observable<SkateDTO> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders().set('Authorization', `Bearer ${token}`) : new HttpHeaders();
    return this.http.get<SkateDTO>(`${this.apiUrl}/${id}`, { headers });
  }

  createSkate(skate: SkateDTO): Observable<SkateDTO> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders().set('Authorization', `Bearer ${token}`) : new HttpHeaders();
    return this.http.post<SkateDTO>(this.apiUrl, skate, { headers });
  }

  updateSkate(id: number, skate: SkateDTO): Observable<SkateDTO> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders().set('Authorization', `Bearer ${token}`) : new HttpHeaders();
    return this.http.put<SkateDTO>(`${this.apiUrl}/${id}`, skate, { headers });
  }

  deleteSkate(id: number): Observable<void> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders().set('Authorization', `Bearer ${token}`) : new HttpHeaders();
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers });
  }
}
