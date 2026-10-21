import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Deja entrar solo si hay sesión. Si no, manda a /login.
 * Se usa en las rutas privadas (/app/...). Solo esconde pantallas: la seguridad real está en el back.
 */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.autenticado() ? true : router.createUrlTree(['/login']);
};
