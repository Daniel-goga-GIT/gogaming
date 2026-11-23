import { Buscar } from './features/buscar/buscar';
import { Routes } from '@angular/router'
import { Home } from './features/home/home'
import { About } from './features/about/about'
import { Login } from './features/login/login'
import { Register } from './features/register/register'
import { Forbidden } from './features/forbidden/forbidden';
import { Error404 } from './features/error404/error404';
import { Dashboard } from './features/dashboard/dashboard';
import { Twofactor } from './features/twofactor/twofactor'
import { Me } from './features/me/me';
import { Cart } from './features/cart/cart';
import { PagoCompletado } from './features/pago-completado/pago-completado';

// Ratones
import { Ratones } from './features/productos/ratones/ratones';
import { RatonDetail } from './features/productos/ratones/raton-detail/raton-detail';

// Teclados
import { Teclados } from './features/productos/teclados/teclados';
import { TecladoDetail } from './features/productos/teclados/teclado-detail/teclado-detail';

// Skates
import { Skates } from './features/productos/skates/skates';
import { SkateDetail } from './features/productos/skates/skate-detail/skate-detail';

// Alfombrillas
import { Alfombrillas } from './features/productos/alfombrillas/alfombrillas';
import { AlfombrillaDetail } from './features/productos/alfombrillas/alfombrilla-detail/alfombrilla-detail';

// Auriculares
import { Auriculares } from './features/productos/auriculares/auriculares';
import { AuricularDetail } from './features/productos/auriculares/auricular-detail/auricular-detail';

// Mangas
import { Mangas } from './features/productos/mangas/mangas';
import { MangaDetail } from './features/productos/mangas/manga-detail/manga-detail';


import { authGuard } from './core/guards/auth-guard';
import { authGuardLogin } from './core/guards/auth-login';


export const routes: Routes = [
  { path: '', component: Home },
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
  { path: 'twofactor', component: Twofactor },
  { path: 'about', component: About },
  { path: 'buscar', component: Buscar },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  

  // Ratones
  { path: 'ratones', component: Ratones },
  { path: 'productos/raton/:id', component: RatonDetail },

  // Teclados
  { path: 'teclados', component: Teclados },
  { path: 'productos/teclado/:id', component: TecladoDetail },

  // Skates
  { path: 'skates', component: Skates },
  { path: 'productos/skate/:id', component: SkateDetail },

  // Alfombrillas
  { path: 'alfombrillas', component: Alfombrillas },
  { path: 'productos/alfombrilla/:id', component: AlfombrillaDetail },

  // Auriculares (ruta del header: /iem-cascos)
  { path: 'iem-cascos', component: Auriculares },
  { path: 'productos/auriculares/:id', component: AuricularDetail },

  // Mangas
  { path: 'mangas', component: Mangas },
  { path: 'productos/manga/:id', component: MangaDetail },

  // Carrito y pagos
  { path: 'cesta', component: Cart },
  { path: 'pago-completado', component: PagoCompletado, canActivate: [authGuardLogin] },

  // Perfil
  { path: 'me', component: Me, canActivate: [authGuardLogin] },

  // Errores
  { path: 'forbidden', component: Forbidden },
  { path: '**', component: Error404 }
]
