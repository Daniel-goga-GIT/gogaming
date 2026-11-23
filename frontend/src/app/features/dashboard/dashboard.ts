import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { RatonService } from '../../core/services/raton.service';
import { TecladoService } from '../../core/services/teclado.service';
import { SkateService } from '../../core/services/skate.service';
import { AlfombrillaService } from '../../core/services/alfombrilla.service';
import { AuricularesService } from '../../core/services/auriculares.service';
import { MangaService } from '../../core/services/manga.service';
import { TicketService, TicketResponse } from '../../core/services/ticket.service';
import {
  RatonDTO, TecladoDTO, SkateDTO,
  AlfombrillaDTO, AuricularesDTO, MangaDTO
} from '../../models/types';
import { TranslateModule } from '@ngx-translate/core';

type Section = 'ratones' | 'teclados' | 'skates' | 'alfombrillas' | 'auriculares' | 'mangas' | 'pedidos';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, TranslateModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {
  sidebarOpen = false;
  activeSection: Section = 'ratones';
  loading = false;
  error: string | null = null;
  showForm = false;

  ratones: RatonDTO[] = [];
  editingRaton: RatonDTO | null = null;
  ratonForm: RatonDTO = this.emptyRaton();

  teclados: TecladoDTO[] = [];
  editingTeclado: TecladoDTO | null = null;
  tecladoForm: TecladoDTO = this.emptyTeclado();

  skates: SkateDTO[] = [];
  editingSkate: SkateDTO | null = null;
  skateForm: SkateDTO = this.emptySkate();

  alfombrillas: AlfombrillaDTO[] = [];
  editingAlfombrilla: AlfombrillaDTO | null = null;
  alfombrillaForm: AlfombrillaDTO = this.emptyAlfombrilla();

  auriculares: AuricularesDTO[] = [];
  editingAuriculares: AuricularesDTO | null = null;
  auricularesForm: AuricularesDTO = this.emptyAuriculares();

  mangas: MangaDTO[] = [];
  editingManga: MangaDTO | null = null;
  mangaForm: MangaDTO = this.emptyManga();

  pedidos: TicketResponse[] = [];

  constructor(
    private ratonService: RatonService,
    private tecladoService: TecladoService,
    private skateService: SkateService,
    private alfombrillaService: AlfombrillaService,
    private auricularesService: AuricularesService,
    private mangaService: MangaService,
    private ticketService: TicketService
  ) {}

  ngOnInit(): void {
    this.loadSection('ratones');
  }

  switchSection(section: Section): void {
    this.activeSection = section;
    this.showForm = false;
    this.error = null;
    this.sidebarOpen = false;
    this.loadSection(section);
  }

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }

  closeForm(): void {
    this.showForm = false;
  }

  getImageUrl(url: string | undefined): string {
    if (!url) return 'raton.png';
    return url.replace(/"/g, '').trim();
  }

  private loadSection(section: Section): void {
    this.loading = true;
    this.error = null;
    switch (section) {
      case 'ratones':      this.loadRatones(); break;
      case 'teclados':     this.loadTeclados(); break;
      case 'skates':       this.loadSkates(); break;
      case 'alfombrillas': this.loadAlfombrillas(); break;
      case 'auriculares':  this.loadAuriculares(); break;
      case 'mangas':       this.loadMangas(); break;
      case 'pedidos':      this.loadPedidos(); break;
    }
  }

  // ── RATONES ──────────────────────────────────────────────────────────────

  private loadRatones(): void {
    this.ratonService.fetchRatones(0, 100, 'id', 'asc').subscribe({
      next: (res) => {
        this.ratones = res.content;
        this.loading = false;
      },
      error: () => {
        this.error = 'Error al cargar los ratones';
        this.loading = false;
      }
    });
  }

  openCreateRaton(): void {
    this.editingRaton = null;
    this.ratonForm = this.emptyRaton();
    this.showForm = true;
  }

  openEditRaton(r: RatonDTO): void {
    this.editingRaton = r;
    this.ratonForm = { ...r };
    this.showForm = true;
  }

  saveRaton(): void {
    const op = this.editingRaton
      ? this.ratonService.updateRaton(this.editingRaton.id, this.ratonForm)
      : this.ratonService.createRaton(this.ratonForm);
    op.subscribe({
      next: () => {
        this.loadRatones();
        this.closeForm();
      },
      error: () => alert('Error al guardar el ratón')
    });
  }

  deleteRaton(id: number): void {
    if (!confirm('¿Eliminar este ratón?')) return;
    this.ratonService.deleteRaton(id).subscribe({
      next: () => this.loadRatones(),
      error: () => alert('Error al eliminar el ratón')
    });
  }

  private emptyRaton(): RatonDTO {
    return {
      id: 0, nombre: '', marca: '',
      fechaPublicacion: new Date().toISOString().split('T')[0] as any,
      precio: 0, stock: 0, tipoProducto: 'Raton', image: '',
      tipoAgarre: 'Palm', medidas: '', sensor: '', peso: 0, switches: ''
    };
  }

  // ── TECLADOS ─────────────────────────────────────────────────────────────

  private loadTeclados(): void {
    this.tecladoService.fetchTeclados(0, 100, 'id', 'asc').subscribe({
      next: (res) => {
        this.teclados = res.content;
        this.loading = false;
      },
      error: () => {
        this.error = 'Error al cargar los teclados';
        this.loading = false;
      }
    });
  }

  openCreateTeclado(): void {
    this.editingTeclado = null;
    this.tecladoForm = this.emptyTeclado();
    this.showForm = true;
  }

  openEditTeclado(t: TecladoDTO): void {
    this.editingTeclado = t;
    this.tecladoForm = { ...t };
    this.showForm = true;
  }

  saveTeclado(): void {
    const op = this.editingTeclado
      ? this.tecladoService.updateTeclado(this.editingTeclado.id, this.tecladoForm)
      : this.tecladoService.createTeclado(this.tecladoForm);
    op.subscribe({
      next: () => {
        this.loadTeclados();
        this.closeForm();
      },
      error: () => alert('Error al guardar el teclado')
    });
  }

  deleteTeclado(id: number): void {
    if (!confirm('¿Eliminar este teclado?')) return;
    this.tecladoService.deleteTeclado(id).subscribe({
      next: () => this.loadTeclados(),
      error: () => alert('Error al eliminar el teclado')
    });
  }

  private emptyTeclado(): TecladoDTO {
    return {
      id: 0, nombre: '', marca: '',
      fechaPublicacion: new Date().toISOString().split('T')[0] as any,
      precio: 0, stock: 0, tipoProducto: 'Teclado', image: '',
      tipoSwitch: '', layout: '', conectividad: '', retroiluminacion: ''
    };
  }

  // ── SKATES ───────────────────────────────────────────────────────────────

  private loadSkates(): void {
    this.skateService.fetchSkates(0, 100, 'id', 'asc').subscribe({
      next: (res) => {
        this.skates = res.content;
        this.loading = false;
      },
      error: () => {
        this.error = 'Error al cargar los skates';
        this.loading = false;
      }
    });
  }

  openCreateSkate(): void {
    this.editingSkate = null;
    this.skateForm = this.emptySkate();
    this.showForm = true;
  }

  openEditSkate(s: SkateDTO): void {
    this.editingSkate = s;
    this.skateForm = { ...s };
    this.showForm = true;
  }

  saveSkate(): void {
    const op = this.editingSkate
      ? this.skateService.updateSkate(this.editingSkate.id, this.skateForm)
      : this.skateService.createSkate(this.skateForm);
    op.subscribe({
      next: () => {
        this.loadSkates();
        this.closeForm();
      },
      error: () => alert('Error al guardar el skate')
    });
  }

  deleteSkate(id: number): void {
    if (!confirm('¿Eliminar este skate?')) return;
    this.skateService.deleteSkate(id).subscribe({
      next: () => this.loadSkates(),
      error: () => alert('Error al eliminar el skate')
    });
  }

  private emptySkate(): SkateDTO {
    return {
      id: 0, nombre: '', marca: '',
      fechaPublicacion: new Date().toISOString().split('T')[0] as any,
      precio: 0, stock: 0, tipoProducto: 'Skate', image: '',
      material: '', grosor: 0, compatibilidad: ''
    };
  }

  // ── ALFOMBRILLAS ─────────────────────────────────────────────────────────

  private loadAlfombrillas(): void {
    this.alfombrillaService.fetchAlfombrillas(0, 100, 'id', 'asc').subscribe({
      next: (res) => {
        this.alfombrillas = res.content;
        this.loading = false;
      },
      error: () => {
        this.error = 'Error al cargar las alfombrillas';
        this.loading = false;
      }
    });
  }

  openCreateAlfombrilla(): void {
    this.editingAlfombrilla = null;
    this.alfombrillaForm = this.emptyAlfombrilla();
    this.showForm = true;
  }

  openEditAlfombrilla(a: AlfombrillaDTO): void {
    this.editingAlfombrilla = a;
    this.alfombrillaForm = { ...a };
    this.showForm = true;
  }

  saveAlfombrilla(): void {
    const op = this.editingAlfombrilla
      ? this.alfombrillaService.updateAlfombrilla(this.editingAlfombrilla.id, this.alfombrillaForm)
      : this.alfombrillaService.createAlfombrilla(this.alfombrillaForm);
    op.subscribe({
      next: () => {
        this.loadAlfombrillas();
        this.closeForm();
      },
      error: () => alert('Error al guardar la alfombrilla')
    });
  }

  deleteAlfombrilla(id: number): void {
    if (!confirm('¿Eliminar esta alfombrilla?')) return;
    this.alfombrillaService.deleteAlfombrilla(id).subscribe({
      next: () => this.loadAlfombrillas(),
      error: () => alert('Error al eliminar la alfombrilla')
    });
  }

  private emptyAlfombrilla(): AlfombrillaDTO {
    return {
      id: 0, nombre: '', marca: '',
      fechaPublicacion: new Date().toISOString().split('T')[0] as any,
      precio: 0, stock: 0, tipoProducto: 'Alfombrilla', image: '',
      dimensiones: '', grosor: 0, superficie: ''
    };
  }

  // ── AURICULARES ──────────────────────────────────────────────────────────

  private loadAuriculares(): void {
    this.auricularesService.fetchAuriculares(0, 100, 'id', 'asc').subscribe({
      next: (res) => {
        this.auriculares = res.content;
        this.loading = false;
      },
      error: () => {
        this.error = 'Error al cargar los auriculares';
        this.loading = false;
      }
    });
  }

  openCreateAuriculares(): void {
    this.editingAuriculares = null;
    this.auricularesForm = this.emptyAuriculares();
    this.showForm = true;
  }

  openEditAuriculares(a: AuricularesDTO): void {
    this.editingAuriculares = a;
    this.auricularesForm = { ...a };
    this.showForm = true;
  }

  saveAuriculares(): void {
    const op = this.editingAuriculares
      ? this.auricularesService.updateAuriculares(this.editingAuriculares.id, this.auricularesForm)
      : this.auricularesService.createAuriculares(this.auricularesForm);
    op.subscribe({
      next: () => {
        this.loadAuriculares();
        this.closeForm();
      },
      error: () => alert('Error al guardar los auriculares')
    });
  }

  deleteAuriculares(id: number): void {
    if (!confirm('¿Eliminar estos auriculares?')) return;
    this.auricularesService.deleteAuriculares(id).subscribe({
      next: () => this.loadAuriculares(),
      error: () => alert('Error al eliminar los auriculares')
    });
  }

  private emptyAuriculares(): AuricularesDTO {
    return {
      id: 0, nombre: '', marca: '',
      fechaPublicacion: new Date().toISOString().split('T')[0] as any,
      precio: 0, stock: 0, tipoProducto: 'Auriculares', image: '',
      tipo: '', driver: '', conectividad: '', impedancia: ''
    };
  }

  // ── MANGAS ───────────────────────────────────────────────────────────────

  private loadMangas(): void {
    this.mangaService.fetchMangas(0, 100, 'id', 'asc').subscribe({
      next: (res) => {
        this.mangas = res.content;
        this.loading = false;
      },
      error: () => {
        this.error = 'Error al cargar las mangas';
        this.loading = false;
      }
    });
  }

  openCreateManga(): void {
    this.editingManga = null;
    this.mangaForm = this.emptyManga();
    this.showForm = true;
  }

  openEditManga(m: MangaDTO): void {
    this.editingManga = m;
    this.mangaForm = { ...m };
    this.showForm = true;
  }

  saveManga(): void {
    const op = this.editingManga
      ? this.mangaService.updateManga(this.editingManga.id, this.mangaForm)
      : this.mangaService.createManga(this.mangaForm);
    op.subscribe({
      next: () => {
        this.loadMangas();
        this.closeForm();
      },
      error: () => alert('Error al guardar la manga')
    });
  }

  deleteManga(id: number): void {
    if (!confirm('¿Eliminar esta manga?')) return;
    this.mangaService.deleteManga(id).subscribe({
      next: () => this.loadMangas(),
      error: () => alert('Error al eliminar la manga')
    });
  }

  private emptyManga(): MangaDTO {
    return {
      id: 0, nombre: '', marca: '',
      fechaPublicacion: new Date().toISOString().split('T')[0] as any,
      precio: 0, stock: 0, tipoProducto: 'Manga', image: '',
      talla: '', material: '', largo: 0, color: ''
    };
  }

  // ── PEDIDOS ──────────────────────────────────────────────────────────────

  private loadPedidos(): void {
    this.ticketService.getAllTickets().subscribe({
      next: (res) => {
        this.pedidos = res;
        this.loading = false;
      },
      error: () => {
        this.error = 'Error al cargar los pedidos';
        this.loading = false;
      }
    });
  }

}
