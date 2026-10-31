import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Registro } from './pages/registro/registro';
import { RestablecerContrasena } from './pages/restablecer-contrasena/restablecer-contrasena';
import { SolicitarRecuperacion } from './pages/solicitar-recuperacion/solicitar-recuperacion';

// Rutas públicas de la feature auth (HU-01: login y registro · HU-03: recuperar y restablecer).
export const AUTH_ROUTES: Routes = [
  { path: 'login', component: Login },
  { path: 'registro', component: Registro },
  { path: 'recuperar', component: SolicitarRecuperacion },
  { path: 'restablecer', component: RestablecerContrasena },
];
