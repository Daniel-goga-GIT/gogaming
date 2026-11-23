import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MangaDTO } from '../../models/types';
import { environment } from '../../../environments/environments';
import { AuthService } from './auth';

@Injectable({
  providedIn: 'root'
})
export class MangaService {
  private apiUrl = `${environment.apiUrl}/v1/productos/manga`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  fetchMangas(page: number, size: number, sortColumn: string, sortDirection: string, nombre?: string): Observable<any> {
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

  getMangaById(id: number): Observable<MangaDTO> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http.get<MangaDTO>(`${this.apiUrl}/${id}`, { headers });
  }

  createManga(manga: MangaDTO): Observable<MangaDTO> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http.post<MangaDTO>(this.apiUrl, manga, { headers });
  }

  updateManga(id: number, manga: MangaDTO): Observable<MangaDTO> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http.put<MangaDTO>(`${this.apiUrl}/${id}`, manga, { headers });
  }

  deleteManga(id: number): Observable<void> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers });
  }
}
