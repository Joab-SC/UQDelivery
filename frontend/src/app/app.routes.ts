import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { roleGuard } from './core/auth/role.guard';
import { PrivateLayout } from './core/layout/private-layout/private-layout';
import { PublicLayout } from './core/layout/public-layout/public-layout';

/**
 * Rutas de la aplicación. Cada feature tiene su propio archivo xxx.routes.ts
 * y aquí solo se agrega una línea que lo carga (loadChildren carga la feature cuando se visita).
 */
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },

  // Pantallas sin sesión
  {
    path: '',
    component: PublicLayout,
    children: [
      { path: '', loadChildren: () => import('./features/auth/auth.routes').then((m) => m.AUTH_ROUTES) },
    ],
  },

  // Panel con sesión
  {
    path: 'app',
    component: PrivateLayout,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'inicio' },
      { path: 'inicio', loadChildren: () => import('./features/inicio/inicio.routes').then((m) => m.INICIO_ROUTES) },
      {
        path: 'usuarios',
        canActivate: [roleGuard('ADMIN')],
        loadChildren: () => import('./features/usuarios/usuarios.routes').then((m) => m.USUARIOS_ROUTES),
      },
      {
        path: 'envios',
        canActivate: [roleGuard('EMISOR')],
        loadChildren: () => import('./features/envios/envios.routes').then((m) => m.ENVIOS_ROUTES),
      },
    ],
  },

  // Cualquier otra ruta: al inicio (si no hay sesión, el guard manda a /login)
  { path: '**', redirectTo: 'app/inicio' },
];
