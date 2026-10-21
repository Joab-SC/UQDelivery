import { Routes } from '@angular/router';
import { Login } from './pages/login/login';

// Rutas públicas de la feature auth. HU-01 agrega /registro y HU-03 /recuperar y /restablecer.
export const AUTH_ROUTES: Routes = [{ path: 'login', component: Login }];
