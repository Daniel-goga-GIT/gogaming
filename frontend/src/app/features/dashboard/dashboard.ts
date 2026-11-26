import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, NgClass, NgTemplateOutlet } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { CATEGORIAS, Categoria, SlugCategoria, categoriaPorSlug, urlImagen } from '../../core/catalogo';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { NotificationService } from '../../core/services/notification.service';
import { ProductoService } from '../../core/services/producto.service';
import { TicketResponse, TicketService } from '../../core/services/ticket.service';
import { ProductoDTO } from '../../models/types';
import { PricePipe } from '../../shared/pipes/price.pipe';

type Seccion = SlugCategoria | 'pedidos';
/** Producto en edición: campos comunes más los de su categoría. */
type Formulario = Record<string, string | number | null>;

/**
 * Panel de administración: gestión del catálogo (las 6 categorías con la misma tabla y el mismo
 * formulario, generados a partir de core/catalogo.ts) y listado de pedidos.
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [PricePipe, DatePipe, NgClass, NgTemplateOutlet, RouterModule, FormsModule, TranslateModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  private productoService = inject(ProductoService);
  private ticketService = inject(TicketService);
  private notify = inject(NotificationService);
  private confirmDialog = inject(ConfirmDialogService);
  private translate = inject(TranslateService);

  protected readonly categorias = CATEGORIAS;
  protected readonly urlImagen = urlImagen;
  protected readonly iconoPedidos = 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01';

  protected readonly seccion = signal<Seccion>('raton');
  protected readonly productos = signal<ProductoDTO[]>([]);
  protected readonly pedidos = signal<TicketResponse[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal(false);
  protected readonly guardando = signal(false);
  protected sidebarOpen = false;

  /** Paginación y búsqueda del catálogo. */
  protected static readonly TAMANO_PAGINA = 20;
  protected pagina = 0;
  protected busqueda = '';
  protected readonly totalPaginas = signal(0);
  protected readonly totalProductos = signal(0);

  /** Pedidos: estados que puede poner el admin (los mismos que acepta el backend) y fila desplegada. */
  protected readonly estados = ['Preparando', 'Enviado', 'Entregado'];
  protected readonly pedidoAbierto = signal<number | null>(null);

  /** Producto que se está creando o editando (null = se muestra la tabla). */
  protected formulario: Formulario | null = null;
  protected editandoId: number | null = null;

  ngOnInit(): void {
    this.cambiarSeccion('raton');
  }

  protected get categoria(): Categoria | undefined {
    return categoriaPorSlug(this.seccion());
  }

  protected cambiarSeccion(seccion: Seccion): void {
    this.seccion.set(seccion);
    this.cerrarFormulario();
    this.sidebarOpen = false;
    this.pagina = 0;
    this.busqueda = '';
    this.cargar();
  }

  private cargar(): void {
    this.loading.set(true);
    this.error.set(false);
    const fin = { error: () => { this.error.set(true); this.loading.set(false); } };
    const categoria = this.categoria;
    if (!categoria) {
      this.ticketService.getAllTickets().subscribe({
        // Los más recientes primero.
        next: p => { this.pedidos.set([...p].sort((a, b) => b.fecha.localeCompare(a.fecha))); this.loading.set(false); },
        ...fin,
      });
      return;
    }
    this.productoService
      .listar(categoria.slug, { page: this.pagina, size: Dashboard.TAMANO_PAGINA, nombre: this.busqueda, sort: 'id', direction: 'desc' })
      .subscribe({
        next: pagina => {
          this.productos.set(pagina.content);
          this.totalPaginas.set(pagina.totalPages);
          this.totalProductos.set(pagina.totalElements);
          this.loading.set(false);
        },
        ...fin,
      });
  }

  protected buscar(): void {
    this.pagina = 0;
    this.cargar();
  }

  protected irAPagina(pagina: number): void {
    if (pagina >= 0 && pagina < this.totalPaginas()) {
      this.pagina = pagina;
      this.cargar();
    }
  }

  protected alternarPedido(id: number): void {
    this.pedidoAbierto.update(actual => (actual === id ? null : id));
  }

  protected cambiarEstado(pedido: TicketResponse, estado: string): void {
    if (estado === pedido.estado) return;
    this.ticketService.updateEstado(pedido.id, estado).subscribe({
      next: actualizado => {
        this.pedidos.update(lista => lista.map(p => (p.id === actualizado.id ? actualizado : p)));
        this.notify.success(this.translate.instant('ADMIN.STATUS_UPDATED', {
          number: pedido.numeroTicket,
          status: this.translate.instant('ORDER_STATUS.' + estado),
        }));
      },
      error: () => {
        this.cargar(); // para que el selector vuelva a mostrar el estado real
        this.notify.error(this.translate.instant('ADMIN.STATUS_ERROR'));
      },
    });
  }

  /** Texto secundario de cada fila de la tabla (sensor, layout...). */
  protected resumen(producto: ProductoDTO): string {
    const datos = producto as unknown as Record<string, unknown>;
    return (this.categoria?.resumen ?? []).map(clave => datos[clave]).filter(Boolean).join(' — ');
  }

  protected abrirNuevo(): void {
    const categoria = this.categoria!;
    const formulario: Formulario = {
      nombre: '', marca: '', precio: null, stock: null, image: '',
      fechaPublicacion: new Date().toISOString().slice(0, 10),
    };
    categoria.campos.forEach(c => (formulario[c.clave] = c.opciones?.[0] ?? (c.tipo === 'numero' ? null : '')));
    this.editandoId = null;
    this.formulario = formulario;
  }

  protected abrirEditar(producto: ProductoDTO): void {
    this.editandoId = producto.id;
    this.formulario = { ...(producto as unknown as Formulario) };
  }

  protected cerrarFormulario(): void {
    this.formulario = null;
    this.editandoId = null;
  }

  protected guardar(): void {
    const categoria = this.categoria;
    if (!categoria || !this.formulario || this.guardando()) return;
    const datos = this.formulario as Partial<ProductoDTO>;
    const peticion = this.editandoId
      ? this.productoService.actualizar(categoria.slug, this.editandoId, datos)
      : this.productoService.crear(categoria.slug, datos);

    this.guardando.set(true);
    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.cerrarFormulario();
        this.cargar();
        this.notify.success(this.translate.instant('ADMIN.SAVED'));
      },
      error: err => {
        this.guardando.set(false);
        // El backend devuelve la lista de errores de validación en err.error.errors.
        const errores: string[] | undefined = err?.error?.errors;
        this.notify.error(this.translate.instant('ADMIN.SAVE_ERROR'), {
          message: errores?.length ? errores.join('. ') : this.translate.instant('ADMIN.SAVE_ERROR_MSG'),
        });
      },
    });
  }

  protected async eliminar(producto: ProductoDTO): Promise<void> {
    const categoria = this.categoria;
    if (!categoria) return;
    const confirmado = await this.confirmDialog.confirm({
      title: this.translate.instant('ADMIN.DELETE_TITLE', { name: producto.nombre }),
      message: this.translate.instant('ADMIN.DELETE_MSG'),
      confirmText: this.translate.instant('ADMIN.DELETE'),
      variant: 'danger',
    });
    if (!confirmado) return;

    this.productoService.eliminar(categoria.slug, producto.id).subscribe({
      next: () => {
        this.cargar();
        this.notify.success(this.translate.instant('ADMIN.DELETED'));
      },
      error: err => this.notify.error(this.translate.instant('ADMIN.DELETE_ERROR'), {
        message: this.translate.instant(err?.status === 409 ? 'ADMIN.DELETE_IN_USE' : 'ADMIN.SAVE_ERROR_MSG'),
      }),
    });
  }
}
