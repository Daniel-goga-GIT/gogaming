import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { Component, signal } from '@angular/core'
import { OnInit, OnDestroy, ChangeDetectorRef, inject } from '@angular/core';
import { AuthService } from '../../core/services/auth';
import { RouterLink, Router, RouterLinkActive, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { UserService } from '../../core/services/user';
import { CartService } from '../../core/services/cart.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { UserDTO } from '../../models/types';
import { environment } from '../../../environments/environments';

@Component({
  selector: 'app-header',
  imports: [RouterLinkActive, RouterLink, CommonModule, TranslateModule],
  templateUrl: './header.html',
  styleUrls: ['./header.css'],
})
export class Header implements OnInit, OnDestroy {
  private confirmDialog = inject(ConfirmDialogService);
  private router = inject(Router);
  protected searchTerm = '';

  isLoggedIn: boolean = false;
  isAdmin: boolean = false;
  isMobileMenuOpen: boolean = false;
  user: UserDTO = new UserDTO();
  cartCount: number = 0;
  get currentLang(): string {
    return this.translate.currentLang || 'es';
  }
  private subscription: Subscription | null = null;
  private cartSubscription: Subscription | null = null;

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private cartService: CartService,
    private translate: TranslateService,
    private cdr: ChangeDetectorRef
  ) {}

  switchLanguage(lang: string) {
    this.translate.use(lang);
  }

  ngOnInit() {
    this.router.events.subscribe(e => {
      if (e instanceof NavigationEnd) {
        const url = this.router.parseUrl(e.urlAfterRedirects);
        this.searchTerm = e.urlAfterRedirects.startsWith('/buscar') ? (url.queryParams['q'] ?? '') : '';
      }
    });
    this.subscription = this.authService.isLoggedIn().subscribe((loggedIn) => {
      this.isLoggedIn = loggedIn;
      if (loggedIn) {
        this.checkAdminRole();
      } else {
        this.isAdmin = false;
      }
    });

    this.cartSubscription = this.cartService.cartItems$.subscribe(items => {
      this.cartCount = items.reduce((count, item) => count + item.quantity, 0);
      this.cdr.detectChanges();
    });
  }

  checkAdminRole() {
    this.isAdmin = this.authService.isAdmin();
  }

  ngAfterViewInit() {
    this.authService.isLoggedIn().subscribe((loggedIn) => {
      if (loggedIn) {
        this.userService.fetchUser().subscribe((res: UserDTO) => {
          this.user.firstName = res.firstName;
          this.user.lastName = res.lastName.charAt(0).toUpperCase() + '.';
          this.user.image = res.image ? `${environment.apiUrl}/v1/users/image/${res.image}` : 'https://cdn-icons-png.flaticon.com/512/6326/6326055.png';
          this.cdr.detectChanges();
        });
        this.checkAdminRole();
      }
    });
  }

  menuOpen = signal(false)
  toggleMenu() {
    this.menuOpen.update(v => !v);
    this.cdr.detectChanges();
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
    if (this.cartSubscription) {
      this.cartSubscription.unsubscribe();
    }
  }

  async openLogoutConfirm() {
    const ok = await this.confirmDialog.confirm({
      title: this.translate.instant('HEADER.LOGOUT_CONFIRM_TITLE'),
      message: this.translate.instant('HEADER.LOGOUT_CONFIRM_MSG'),
      confirmText: this.translate.instant('HEADER.LOGOUT'),
      cancelText: this.translate.instant('COMMON.CANCEL'),
    });
    if (ok) {
      this.authService.logout();
      this.cdr.detectChanges();
    }
  }

  /** Buscador global de la cabecera. */
  search(term: string) {
    const q = term.trim();
    if (!q) return;
    this.searchTerm = q;
    this.menuOpen.set(false);
    this.router.navigate(['/buscar'], { queryParams: { q } });
  }
}
