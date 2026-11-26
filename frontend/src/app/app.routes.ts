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

import { ListadoProductos } from './features/productos/listado/listado-productos';
import { DetalleProducto } from './features/productos/detalle/detalle-producto';
import { CATEGORIAS } from './core/catalogo';

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


  // Catálogo: un listado por categoría (/ratones, /teclados, ...) y una ficha común.
  ...CATEGORIAS.map(c => ({ path: c.ruta.slice(1), component: ListadoProductos, data: { categoria: c.slug } })),
  { path: 'productos/:categoria/:id', component: DetalleProducto },

  // Carrito y pagos
  { path: 'cesta', component: Cart },
  { path: 'pago-completado', component: PagoCompletado, canActivate: [authGuardLogin] },

  // Perfil
  { path: 'me', component: Me, canActivate: [authGuardLogin] },

  // Errores
  { path: 'forbidden', component: Forbidden },
  { path: '**', component: Error404 }
]
