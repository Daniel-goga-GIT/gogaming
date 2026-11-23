import { Component, signal, OnInit, OnDestroy, inject } from '@angular/core'
import { RouterOutlet, Router, NavigationEnd, Event } from '@angular/router'
import { Header } from './layout/header/header'
import { Footer } from './layout/footer/footer'
import { AiChatComponent } from './layout/ai-chat/ai-chat.component'
import { CommonModule } from '@angular/common'
import { filter } from 'rxjs/operators'
import { Subscription } from 'rxjs'
import { TranslateModule } from '@ngx-translate/core'

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Header, Footer, AiChatComponent, CommonModule, TranslateModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit, OnDestroy {
  protected readonly title = signal('gogaming')
  showFooter: boolean = true;
  private router = inject(Router);
  private routerSubscription: Subscription | null = null;

  ngOnInit() {
    // Check initial URL
    this.showFooter = !this.router.url.startsWith('/dashboard');

    this.routerSubscription = this.router.events.pipe(
      filter((event: Event): event is NavigationEnd => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      // Ocultar footer si la URL comienza con /dashboard
      this.showFooter = !event.urlAfterRedirects.startsWith('/dashboard');
    });
  }

  ngOnDestroy() {
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
  }
}
