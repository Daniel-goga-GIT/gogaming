import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuricularesDTO } from '../../models/types';
import { environment } from '../../../environments/environments';
import { AuthService } from './auth';

@Injectable({
  providedIn: 'root'
})
export class AuricularesService {
  private apiUrl = `${environment.apiUrl}/v1/productos/auriculares`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  fetchAuriculares(page: number, size: number, sortColumn: string, sortDirection: string, nombre?: string): Observable<any> {
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

  getAuricularesById(id: number): Observable<AuricularesDTO> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders().set('Authorization', `Bearer ${token}`) : new HttpHeaders();
    return this.http.get<AuricularesDTO>(`${this.apiUrl}/${id}`, { headers });
  }

  createAuriculares(auriculares: AuricularesDTO): Observable<AuricularesDTO> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders().set('Authorization', `Bearer ${token}`) : new HttpHeaders();
    return this.http.post<AuricularesDTO>(this.apiUrl, auriculares, { headers });
  }

  updateAuriculares(id: number, auriculares: AuricularesDTO): Observable<AuricularesDTO> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders().set('Authorization', `Bearer ${token}`) : new HttpHeaders();
    return this.http.put<AuricularesDTO>(`${this.apiUrl}/${id}`, auriculares, { headers });
  }

  deleteAuriculares(id: number): Observable<void> {
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders().set('Authorization', `Bearer ${token}`) : new HttpHeaders();
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers });
  }
}
