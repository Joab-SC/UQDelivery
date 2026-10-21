import { Routes } from '@angular/router';
import { Inicio } from './pages/inicio/inicio';

// Rutas de la feature. app.routes.ts las carga bajo /app/inicio.
export const INICIO_ROUTES: Routes = [{ path: '', component: Inicio }];
