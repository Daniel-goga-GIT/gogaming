import { Injectable } from '@angular/core';
import { AuthService } from './auth';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environments';
import { UserCreateDTO } from '../../models/types';

@Injectable({
  providedIn: 'root',
})
export class UserService {

  constructor(private http: HttpClient, private authService: AuthService) {}

  fetchUser(): Observable<any> {
      // Obtener el token de autenticación desde el servicio de autenticación
      const token = this.authService.getToken();
      // Si el usuario no está autenticado, lanzar un error
      if (!token) {
        return throwError(() => new Error('Unauthorized'));
      }
      // Realizar la solicitud GET a la API con autenticación y parámetros de paginación y ordenación
      return this.http.get(`${environment.apiUrl}/v1/users/me`, {
        headers: new HttpHeaders({ 
          Authorization: `Bearer ${token}` }), // Encabezado con el token de autenticación
      });
    }

    /* MÉTODO PARA REGISTRAR UN NUEVO USUARIO */
    register(userCreateDTO: UserCreateDTO): Observable<any> {
      const formData = new FormData();
      formData.append('firstName', userCreateDTO.firstName);
      formData.append('lastName', userCreateDTO.lastName);
      formData.append('email', userCreateDTO.email);
      formData.append('password', userCreateDTO.password);

      if (userCreateDTO.image) {
          formData.append('imageFile', userCreateDTO.image);
      }

      return this.http.post<any>(
          `${environment.apiUrl}/v1/users/register`,
          formData,
          {
              // Forzamos un objeto de opciones vacío para evitar 
              // que interceptores globales inyecten Content-Type
              headers: new HttpHeaders() 
          }
      );
    }

    /* MÉTODO PARA ACTUALIZAR LA IMAGEN DE PERFIL */
    uploadImage(file: File): Observable<any> {
      const formData = new FormData();
      formData.append('image', file); // <--- ESTA CLAVE debe ser igual al @RequestParam("image") de Java

      const token = this.authService.getToken(); 
      const headers = new HttpHeaders({
        'Authorization': `Bearer ${token}`
      });

      return this.http.post(`${environment.apiUrl}/v1/users/me/image`, formData, { headers });
    }
  
}
