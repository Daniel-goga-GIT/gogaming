import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environments';
import { configuration } from '../../config/configuration';
import { jwtDecode } from 'jwt-decode';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  // Inicializamos el BehaviorSubject con el valor que haya en localStorage (si existe)
  private token = new BehaviorSubject<string | null>(localStorage.getItem(configuration.KEY_TOKEN));

  constructor(
    private http: HttpClient, 
    private router: Router,
  ) { }

  login(email: string, password: string): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(
      `${environment.apiUrl}/v1/authenticate`,
      { email, password },
      { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
    );
  }

  twofactor(code: string): Observable<{ token: string }> {
        const token = this.getToken(); // Obtén el token del AuthService
        if (!token) {
            // Si no hay token, lanza un error indicando que el usuario no está autorizado.
            return throwError(() => new Error('Unauthorized'));
        }

        return this.http.post<{ token: string }>(
            `${environment.apiUrl}/v1/twofactor`,
            { code },
            {
                headers: new HttpHeaders({
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ` + token
                })
            }
        ).pipe(
            tap(response => {
                this.setToken(response.token);
            })
        );
    }



  /**
   * Guarda el token en el BehaviorSubject y en el almacenamiento persistente.
   */
  setToken(token: string): void {
    localStorage.setItem(configuration.KEY_TOKEN, token); // Persistencia en el navegador
    this.token.next(token);              // Notifica a los suscriptores
  }

  /**
   * Obtiene el token actual. Primero intenta del Subject, si no, del localStorage.
   */
  getToken(): string | null {
    return localStorage.getItem(configuration.KEY_TOKEN);
  }

  /**
   * Comprueba si el usuario está logueado.
   */
  isLoggedIn(): Observable<boolean> {
    // Verifica si el token existe y emite un valor booleano.
    //Meter tambien la lógica de que si hay token y la variable sea True de que se ha introuducido el código

    return this.token.asObservable().pipe(map((token: string | null) => !!token && localStorage.getItem("twofactor") == "false"));
  }

  getUsername(): string | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const decodedToken: any = jwtDecode(token);
      return decodedToken.sub || null;
    } catch (error) {
      console.error('Error al decodificar el token:', error);
      return null;
    }
    return null;
  }
  
  getRole(): string | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const decodedToken: any = jwtDecode(token);
      return decodedToken.roles[0] || null;
    } catch (error) {
      console.error('Error al decodificar el token:', error);
      return null;
    }
  }

  getUserId(): number | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const decodedToken: any = jwtDecode(token);
      return decodedToken.id ? Number(decodedToken.id) : null;
    } catch (error) {
      console.error('Error al decodificar el token:', error);
      return null;
    }
  }
  
  /**
   * Limpia los datos de sesión y redirige.
   */
  logout(): void {
    localStorage.removeItem(configuration.KEY_TOKEN); // Borra el token persistente
    localStorage.removeItem("twofactor");
    this.token.next(null);          // Notifica que ya no hay usuario
    this.router.navigate(['/']);
  }

  
}


