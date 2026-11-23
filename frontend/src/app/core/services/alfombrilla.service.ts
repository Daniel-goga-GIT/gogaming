import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AlfombrillaDTO } from '../../models/types';
import { environment } from '../../../environments/environments';
import { AuthService } from './auth';

@Injectable({
  providedIn: 'root'
})
export class AlfombrillaService {
  private apiUrl = `${environment.apiUrl}/v1/productos/alfombrilla`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  fetchAlfombrillas(page: number, size: number, sortColumn: string, sortDirection: string, nombre?: string): Observable<any> {
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

  getAlfombrillaById(id: number): Observable<AlfombrillaDTO> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http.get<AlfombrillaDTO>(`${this.apiUrl}/${id}`, { headers });
  }

  createAlfombrilla(alfombrilla: AlfombrillaDTO): Observable<AlfombrillaDTO> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http.post<AlfombrillaDTO>(this.apiUrl, alfombrilla, { headers });
  }

  updateAlfombrilla(id: number, alfombrilla: AlfombrillaDTO): Observable<AlfombrillaDTO> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http.put<AlfombrillaDTO>(`${this.apiUrl}/${id}`, alfombrilla, { headers });
  }

  deleteAlfombrilla(id: number): Observable<void> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers });
  }
}
