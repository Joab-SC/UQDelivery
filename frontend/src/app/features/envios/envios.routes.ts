import { Routes } from '@angular/router';
import { EnvioCreado } from './pages/envio-creado/envio-creado';
import { NuevoEnvio } from './pages/nuevo-envio/nuevo-envio';

// Rutas de la feature. app.routes.ts las carga bajo /app/envios (solo rol EMISOR).
export const ENVIOS_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'nuevo' },
  { path: 'nuevo', component: NuevoEnvio },
  { path: 'creado/:codigo', component: EnvioCreado },
];
