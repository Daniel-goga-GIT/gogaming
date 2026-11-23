import { Component, signal } from '@angular/core'
import { OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { AuthService } from '../../core/services/auth';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { UserService } from '../../core/services/user';
import { CartService } from '../../core/services/cart.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { UserDTO } from '../../models/types';
import { environment } from '../../../environments/environments';

@Component({
  selector: 'app-header',
  imports: [RouterLink, CommonModule, TranslateModule],
  templateUrl: './header.html',
  styleUrls: ['./header.css'],
})
export class Header implements OnInit, OnDestroy {
  isLoggedIn: boolean = false;
  isAdmin: boolean = false;
  isMobileMenuOpen: boolean = false;
  user: UserDTO = new UserDTO();
  showLogoutConfirm: boolean = false;
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
    const role = this.authService.getRole();
    this.isAdmin = role === 'ROLE_ADMIN';
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

  openLogoutConfirm() {
    this.showLogoutConfirm = true;
    this.cdr.detectChanges();
  }

  cancelLogout() {
    this.showLogoutConfirm = false;
    this.cdr.detectChanges();
  }

  confirmLogout() {
    this.showLogoutConfirm = false;
    this.authService.logout();
    this.cdr.detectChanges();
  }
}
