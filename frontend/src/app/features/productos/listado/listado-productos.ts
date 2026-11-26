import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NgTemplateOutlet } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { Categoria, categoriaPorSlug, rutaProducto, urlImagen } from '../../../core/catalogo';
import { ProductoService } from '../../../core/services/producto.service';
import { RatingService } from '../../../core/services/rating.service';
import { ProductoDTO } from '../../../models/types';
import { ProductCard } from '../../../shared/product-card/product-card';

/**
 * Listado de una categoría (/ratones, /teclados...). La categoría llega en data.categoria de la ruta.
 */
@Component({
  selector: 'app-listado-productos',
  standalone: true,
  imports: [ProductCard, FormsModule, NgTemplateOutlet, TranslateModule],
  templateUrl: './listado-productos.html',
})
export class ListadoProductos implements OnInit {
  private route = inject(ActivatedRoute);
  private productoService = inject(ProductoService);
  private ratingService = inject(RatingService);
  private destroyRef = inject(DestroyRef);

  protected readonly rutaProducto = rutaProducto;
  protected readonly urlImagen = urlImagen;

  protected categoria!: Categoria;
  protected readonly productos = signal<ProductoDTO[]>([]);
  protected readonly valoraciones = signal<Record<number, { media: number; total: number }>>({});
  protected readonly totalElements = signal(0);
  protected readonly totalPages = signal(0);
  protected readonly loading = signal(false);
  protected readonly error = signal(false);

  protected currentPage = 0;
  protected readonly pageSize = 10;
  protected sortColumn = 'nombre';
  protected filterValue = '';

  ngOnInit(): void {
    this.route.data.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(data => {
      this.categoria = categoriaPorSlug(data['categoria'])!;
      this.currentPage = 0;
      this.filterValue = '';
      this.cargar();
    });
  }

  protected cargar(): void {
    this.loading.set(true);
    this.error.set(false);
    this.productoService
      .listar(this.categoria.slug, {
        page: this.currentPage,
        size: this.pageSize,
        sort: this.sortColumn,
        nombre: this.filterValue,
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: pagina => {
          this.productos.set(pagina.content);
          this.totalElements.set(pagina.totalElements);
          this.totalPages.set(pagina.totalPages);
          this.currentPage = pagina.number;
          this.loading.set(false);
          this.cargarValoraciones(pagina.content);
        },
        error: () => {
          this.error.set(true);
          this.loading.set(false);
        },
      });
  }

  protected irAPagina(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage = page;
      this.cargar();
    }
  }

  protected buscar(): void {
    this.currentPage = 0;
    this.cargar();
  }

  private cargarValoraciones(productos: ProductoDTO[]): void {
    this.ratingService
      .getSummaries(productos.map(p => p.id))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: resumenes => {
          const valoraciones: Record<number, { media: number; total: number }> = {};
          Object.values(resumenes).forEach(r => (valoraciones[r.productId] = { media: r.averageRating, total: r.totalReviews }));
          this.valoraciones.set(valoraciones);
        },
        error: () => this.valoraciones.set({}),
      });
  }
}
