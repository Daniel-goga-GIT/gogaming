import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environments';
import { Pagina, ProductoDTO } from '../../models/types';
import { SlugCategoria } from '../catalogo';

export interface ConsultaProductos {
  page?: number;
  size?: number;
  /** Campo por el que ordenar: id, nombre, marca, precio, stock o fechaPublicacion. */
  sort?: string;
  direction?: 'asc' | 'desc';
  /** Filtro por nombre o marca. */
  nombre?: string;
}

/** Acceso a la API del catálogo (/api/v1/productos), igual para todas las categorías. */
@Injectable({ providedIn: 'root' })
export class ProductoService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/v1/productos`;

  listar<T extends ProductoDTO = ProductoDTO>(categoria: SlugCategoria, consulta: ConsultaProductos = {}): Observable<Pagina<T>> {
    let params = new HttpParams()
      .set('page', consulta.page ?? 0)
      .set('size', consulta.size ?? 10)
      .set('sort', `${consulta.sort ?? 'id'},${consulta.direction ?? 'asc'}`);
    if (consulta.nombre?.trim()) {
      params = params.set('nombre', consulta.nombre.trim());
    }
    return this.http.get<Pagina<T>>(`${this.apiUrl}/${categoria}`, { params });
  }

  obtener<T extends ProductoDTO = ProductoDTO>(categoria: SlugCategoria, id: number): Observable<T> {
    return this.http.get<T>(`${this.apiUrl}/${categoria}/${id}`);
  }

  /** Un producto de cualquier categoría. */
  obtenerPorId(id: number): Observable<ProductoDTO> {
    return this.http.get<ProductoDTO>(`${this.apiUrl}/${id}`);
  }

  /** Productos destacados de la portada. */
  portada(): Observable<ProductoDTO[]> {
    return this.http.get<ProductoDTO[]>(`${this.apiUrl}/home`);
  }

  crear<T extends ProductoDTO>(categoria: SlugCategoria, producto: Partial<T>): Observable<T> {
    return this.http.post<T>(`${this.apiUrl}/${categoria}`, producto);
  }

  actualizar<T extends ProductoDTO>(categoria: SlugCategoria, id: number, producto: Partial<T>): Observable<T> {
    return this.http.put<T>(`${this.apiUrl}/${categoria}/${id}`, producto);
  }

  eliminar(categoria: SlugCategoria, id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${categoria}/${id}`);
  }
}
