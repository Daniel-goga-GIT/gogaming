import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RatonDTO } from '../../models/types';
import { environment } from '../../../environments/environments';
import { AuthService } from './auth';

@Injectable({
  providedIn: 'root'
})
export class RatonService {
  private apiUrl = `${environment.apiUrl}/v1/productos/raton`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  fetchRatones(page: number, size: number, sortColumn: string, sortDirection: string, nombre?: string): Observable<any> {
    const token = this.authService.getToken();
    
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', `${sortColumn},${sortDirection}`);

    if (nombre) {
      params = params.set('nombre', nombre);
    }

    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

    return this.http.get<any>(this.apiUrl, { params, headers });
  }

  getRatonById(id: number): Observable<RatonDTO> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http.get<RatonDTO>(`${this.apiUrl}/${id}`, { headers });
  }

  createRaton(raton: RatonDTO): Observable<RatonDTO> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http.post<RatonDTO>(this.apiUrl, raton, { headers });
  }

  updateRaton(id: number, raton: RatonDTO): Observable<RatonDTO> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http.put<RatonDTO>(`${this.apiUrl}/${id}`, raton, { headers });
  }

  deleteRaton(id: number): Observable<void> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers });
  }
}
