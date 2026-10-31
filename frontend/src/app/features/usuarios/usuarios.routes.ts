import { Routes } from '@angular/router';
import { GestionUsuarios } from './pages/gestion-usuarios/gestion-usuarios';

// Rutas de la feature. app.routes.ts las carga bajo /app/usuarios (solo rol ADMIN).
export const USUARIOS_ROUTES: Routes = [{ path: '', component: GestionUsuarios }];
