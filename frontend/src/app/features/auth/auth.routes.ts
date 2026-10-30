import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Registro } from './pages/registro/registro';

// Rutas públicas de la feature auth. HU-03 agrega /recuperar y /restablecer.
export const AUTH_ROUTES: Routes = [
  { path: 'login', component: Login },
  { path: 'registro', component: Registro },
];
