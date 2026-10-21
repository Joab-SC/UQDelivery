import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Rol } from '../models/rol';
import { AuthService } from './auth.service';

/**
 * Deja entrar solo a los roles indicados. Si el rol no coincide, manda a /app/inicio.
 * Uso en una ruta: canActivate: [roleGuard('ADMIN')]
 */
export function roleGuard(...roles: Rol[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    const rol = auth.rol();
    return rol !== null && roles.includes(rol) ? true : router.createUrlTree(['/app/inicio']);
  };
}
